package pixy.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import pixy.meta.Metadata;
import pixy.meta.MetadataType;
import pixy.meta.adobe.IPTC_NAA;
import pixy.meta.adobe._8BIM;
import pixy.meta.exif.Exif;
import pixy.meta.exif.ExifTag;
import pixy.meta.iptc.IPTCApplicationTag;
import pixy.meta.iptc.IPTCDataSet;
import pixy.meta.jpeg.JPEGMeta;
import pixy.meta.jpeg.JpegExif;
import pixy.meta.jpeg.JpegXMP;
import pixy.meta.tiff.TiffExif;
import pixy.meta.xmp.XMP;
import pixy.image.tiff.FieldType;
import pixy.image.tiff.TiffTag;
import pixy.util.FileUtils;
import pixy.util.MetadataUtils;
import pixy.string.XMLUtils;

import static org.junit.Assert.assertNotEquals;

public class TestPixyMeta {

	@BeforeClass
	public static void setJvmVersion() {
		if ("9".equals(System.getProperty("java.specification.version"))) {
			System.setProperty("java.specification.version", "1.9");
		}
	}

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	// Obtain a logger instance
	private static final Logger LOGGER = LoggerFactory.getLogger(TestPixyMeta.class);

	private File tmproot() {
		return temporaryFolder.getRoot();
	}
	
	@org.junit.Test
	public void transferJpegXmp() throws IOException {
		File filename = new File("images", "wizard.jpg");
		Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(filename);
		assertNotEquals("metadata", 0, metadataMap.size());
		LOGGER.info("Start of metadata information:");
		LOGGER.info("Total number of metadata entries: {}", metadataMap.size());

		int i = 0;
		for (Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
			LOGGER.info("Metadata entry {} - {}", i, entry.getKey());
			entry.getValue().showMetadata();
			i++;
			LOGGER.info("-----------------------------------------");
		}
		LOGGER.info("End of metadata information.");
		FileInputStream fin = null;
		FileOutputStream fout = null;

		if(metadataMap.get(MetadataType.XMP) != null) {
			XMP xmp = (XMP)metadataMap.get(MetadataType.XMP);
			fin = new FileInputStream("images/1.jpg");
			fout = new FileOutputStream(new File(tmproot(), "1-xmp-inserted.jpg"));
			JpegXMP jpegXmp = null;
			if(!xmp.hasExtendedXmp())
				jpegXmp = new JpegXMP(xmp.getData());
			else {
				Document xmpDoc = xmp.getXmpDocument();
				Document extendedXmpDoc = xmp.getExtendedXmpDocument();
				jpegXmp = new JpegXMP(XMLUtils.serializeToStringLS(xmpDoc, xmpDoc.getDocumentElement()), XMLUtils.serializeToStringLS(extendedXmpDoc));
			}

			Metadata.insertXMP(fin, fout, jpegXmp);

			fin.close();
			fout.close();
		}

	}

	@Test
	public void iptc_envelope_tif() throws IOException {
		FileInputStream fin;
		FileOutputStream fout;
		Metadata.extractThumbnails("images/iptc-envelope.tif", tmproot() + "/iptc-envelope");

		fin = new FileInputStream("images/iptc-envelope.tif");
		fout = new FileOutputStream(new File(tmproot(), "iptc-envelope-iptc-inserted.tif"));

		Metadata.insertIPTC(fin, fout, createIPTCDataSet(), true);

		fin.close();
		fout.close();
	}

	@Test
	public void wizard_jpg() throws Exception {
		FileInputStream fin = new FileInputStream("images/wizard.jpg");
		FileOutputStream fout = new FileOutputStream(new File(tmproot(), "wizard-iptc-inserted.jpg"));

		Metadata.insertIPTC(fin, fout, createIPTCDataSet(), true);

		fin.close();
		fout.close();
	}

	@Test
	public void image1_irbthumbnail_inserted() throws Exception {
		FileInputStream fin = new FileInputStream("images/1.jpg");
		FileOutputStream fout = new FileOutputStream(new File(tmproot(), "1-irbthumbnail-inserted.jpg"));
		
		Metadata.insertIRBThumbnail(fin, fout, createThumbnail("images/1.jpg"));
		
		fin.close();
		fout.close();
	}

	@Test
	public void f1() throws Exception {

		FileInputStream fin = new FileInputStream("images/f1.tif");
		FileOutputStream fout = new FileOutputStream(new File(tmproot(), "f1-irbthumbnail-inserted.tif"));
		
		Metadata.insertIRBThumbnail(fin, fout, createThumbnail("images/f1.tif"));
		
		fin.close();
		fout.close();

	}

	@Test
	public void exif_exif_inserted() throws Exception {
		FileInputStream fin = new FileInputStream("images/exif.tif");
		FileOutputStream fout = new FileOutputStream(new File(tmproot(), "exif-exif-inserted.tif"));
		
		Metadata.insertExif(fin, fout, populateExif(TiffExif.class), true);
		
		fin.close();
		fout.close();

	}

	@Test
	public void image12_insertExif() throws Exception {
		FileInputStream fin = new FileInputStream("images/12.jpg");
		FileOutputStream fout = new FileOutputStream(new File(tmproot(), "12-exif-inserted.jpg"));

		Metadata.insertExif(fin, fout, populateExif(JpegExif.class), true);
		
		fin.close();
		fout.close();

	}

	@Test
	public void image12_removeMetadata() throws Exception {
		FileInputStream fin = new FileInputStream("images/12.jpg");
		File outfile = new File(tmproot(), "12-metadata-removed.jpg");
		FileOutputStream fout = new FileOutputStream(outfile);
		
		Metadata.removeMetadata(fin, fout, MetadataType.JPG_JFIF, MetadataType.JPG_ADOBE, MetadataType.IPTC, MetadataType.ICC_PROFILE, MetadataType.XMP, MetadataType.EXIF);
		
		fin.close();
		fout.close();
		System.out.format("metadata removed from %s%n", outfile);
	}

	@Test
	public void image12_jpg() throws Exception {
		FileInputStream fin = new FileInputStream("images/12.jpg");
		FileOutputStream fout = new FileOutputStream(new File(tmproot(), "12-photoshop-iptc-inserted.jpg"));
		
		Metadata.insertIRB(fin, fout, createPhotoshopIPTC(), true);
		
		fin.close();
		fout.close();

	}

	@Test
	public void table_jpg_depthmap() throws Exception {
		FileInputStream fin = new FileInputStream("images/table.jpg");
		JPEGMeta.extractDepthMap(fin, tmproot() + "/table");
		
		fin.close();

	}

	@Test
	public void butterfly_jpg() throws Exception {
		FileInputStream fin = new FileInputStream("images/butterfly.png");
		FileOutputStream fout = new FileOutputStream(new File(tmproot(), "comment-inserted.png"));
		
		Metadata.insertComments(fin, fout, Arrays.asList("Comment1", "Comment2"));
		
		fin.close();
		fout.close();
	}
	
	private static List<IPTCDataSet> createIPTCDataSet() {
		List<IPTCDataSet> iptcs = new ArrayList<IPTCDataSet>();
		iptcs.add(new IPTCDataSet(IPTCApplicationTag.COPYRIGHT_NOTICE, "Copyright 2014-2016, yuwen_66@yahoo.com"));
		iptcs.add(new IPTCDataSet(IPTCApplicationTag.CATEGORY, "ICAFE"));
		iptcs.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, "Welcome 'icafe' user!"));
		
		return iptcs;
	}
	
	private static List<_8BIM> createPhotoshopIPTC() {
		IPTC_NAA iptc = new IPTC_NAA();
		iptc.addDataSet(new IPTCDataSet(IPTCApplicationTag.COPYRIGHT_NOTICE, "Copyright 2014-2016, yuwen_66@yahoo.com"));
		iptc.addDataSet(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, "Welcome 'icafe' user!"));
		iptc.addDataSet(new IPTCDataSet(IPTCApplicationTag.CATEGORY, "ICAFE"));
		
		return new ArrayList<_8BIM>(Arrays.asList(iptc));
	}
	
	private static BufferedImage createThumbnail(String filePath) throws IOException {
		FileInputStream fin = new FileInputStream(filePath);
		BufferedImage thumbnail = MetadataUtils.createThumbnail(fin);
		
		fin.close();
		
		return thumbnail;
	}
	
	// This method is for testing only
	private static Exif populateExif(Class<?> exifClass) throws IOException {
		// Create an EXIF wrapper
		Exif exif = exifClass == (TiffExif.class)?new TiffExif() : new JpegExif();
		exif.addImageField(TiffTag.WINDOWS_XP_AUTHOR, FieldType.WINDOWSXP, "Author");
		exif.addImageField(TiffTag.WINDOWS_XP_KEYWORDS, FieldType.WINDOWSXP, "Copyright;Author");
		DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		exif.addExifField(ExifTag.EXPOSURE_TIME, FieldType.RATIONAL, new int[] {10, 600});
		exif.addExifField(ExifTag.FNUMBER, FieldType.RATIONAL, new int[] {49, 10});
		exif.addExifField(ExifTag.ISO_SPEED_RATINGS, FieldType.SHORT, new short[]{273});
		//All four bytes should be interpreted as ASCII values - represents [0220] - new byte[]{48, 50, 50, 48}
		exif.addExifField(ExifTag.EXIF_VERSION, FieldType.UNDEFINED, "0220".getBytes());
		exif.addExifField(ExifTag.DATE_TIME_ORIGINAL, FieldType.ASCII, formatter.format(new Date()));
		exif.addExifField(ExifTag.DATE_TIME_DIGITIZED, FieldType.ASCII, formatter.format(new Date()));
		exif.addExifField(ExifTag.FOCAL_LENGTH, FieldType.RATIONAL, new int[] {240, 10});		
		// Insert ThumbNailIFD
		// Since we don't provide thumbnail image, it will be created later from the input stream
		exif.setThumbnailRequired(true);
		
		return exif;
	}
}