package mm.projectV.util;

import lombok.Getter;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ImageProcessor {

    public static final String JPEG_CONTENT_TYPE = "image/jpeg";

    private static final int ORIGINAL_MAX_WIDTH = 1920;
    private static final int CARD_MAX_WIDTH = 800;
    private static final int THUMB_MAX_WIDTH = 200;

    private static final float ORIGINAL_QUALITY = 0.90f;
    private static final float CARD_QUALITY = 0.85f;
    private static final float THUMB_QUALITY = 0.80f;

    public ProcessedImage process(byte[] sourceBytes) throws IOException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(sourceBytes));
        if (source == null) {
            throw new IOException("Unsupported or corrupt image");
        }

        byte[] original = resize(source, ORIGINAL_MAX_WIDTH, ORIGINAL_QUALITY);
        byte[] card = resize(source, CARD_MAX_WIDTH, CARD_QUALITY);
        byte[] thumb = resize(source, THUMB_MAX_WIDTH, THUMB_QUALITY);

        return new ProcessedImage(original, card, thumb, source.getWidth(), source.getHeight());
    }

    private byte[] resize(BufferedImage source, int maxWidth, float quality) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int targetWidth = Math.min(source.getWidth(), maxWidth);
        Thumbnails.of(source)
                .width(targetWidth)
                .outputFormat("jpeg")
                .outputQuality(quality)
                .toOutputStream(out);
        return out.toByteArray();
    }

    @Getter
    public static class ProcessedImage {
        private final byte[] original;
        private final byte[] card;
        private final byte[] thumb;
        private final int sourceWidth;
        private final int sourceHeight;

        public ProcessedImage(byte[] original, byte[] card, byte[] thumb, int sourceWidth, int sourceHeight) {
            this.original = original;
            this.card = card;
            this.thumb = thumb;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
        }
    }
}