package pixy.test;

import org.junit.Test;
import pixy.image.tiff.IFD;
import pixy.meta.Metadata;
import pixy.meta.MetadataType;
import pixy.meta.exif.Exif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class RemoveExifTest {

    @Test
    public void removeExif() throws Exception {
        File wizardFile = new File(getClass().getResource("/Supreme_Court-q50.jpg").toURI());
        Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(wizardFile);
        if (!metadataMap.containsKey(MetadataType.EXIF)) {
            throw new IllegalStateException("expect start metadata to contain exif");
        }
        System.out.format("metadata types: %s%n", metadataMap.keySet());
        Exif exif = (Exif) metadataMap.get(MetadataType.EXIF);
        Stream<IFD> ifds = Stream.of(exif.getExifIFD(), exif.getGPSIFD(), exif.getImageIFD());
        List<String> fieldNames = new ArrayList<>();
        ifds.filter(Objects::nonNull).forEach(ifd -> {
//            ifd.getFields().forEach(field -> {
//                fieldNames.add(field.getType().getName());
//            });
            ifd.getChildren().keySet().forEach(tag -> {
                fieldNames.add(tag.getName());
            });
        });
        System.out.format("fields: %s%n", fieldNames);

    }
}
