package demo.webrtc.utils.videofile;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.RgbToBgr;
import org.jcodec.scale.Transform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kantipud on 28-12-2017.
 */
public class ThumbnailGenerator {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

    public File createThumbnailFromVideo(File file, Type type, int[] framesForThumbnail) throws Exception {
        if (Type.PNG.equals(type)) {
            Picture frame = FrameGrab.getFrameFromFile(file, framesForThumbnail[0]);
            File imgFile = new File(file.getParent() + "\\" + StringUtils.replace(file.getName(), "mp4", "png"));
            ImageIO.write(toBufferedImage8Bit(frame), "png", imgFile);
            return imgFile;
        } else if (Type.GIF.equals(type)) {
            List<BufferedImage> bufferedImages = new ArrayList<>();
            File imgFile = new File(file.getParent() + "\\" + StringUtils.replace(file.getName(), "mp4", "gif"));
            for (int frame : framesForThumbnail) {
                bufferedImages.add(toBufferedImage8Bit(FrameGrab.getFrameFromFile(file, frame)));
            }
            GifSequenceWriter.create(bufferedImages, imgFile);
            return imgFile;
        } else {
            log.error("Invalid output thumbnail type");
            return null;
        }
    }

    // this method is from Jcodec AWTUtils.java.
    private BufferedImage toBufferedImage8Bit(Picture src) {
        if (src.getColor() != ColorSpace.RGB) {
            Transform transform = ColorUtil.getTransform(src.getColor(), ColorSpace.RGB);
            if (transform == null) {
                throw new IllegalArgumentException("Unsupported input colorspace: " + src.getColor());
            }
            Picture out = Picture.create(src.getWidth(), src.getHeight(), ColorSpace.RGB);
            transform.transform(src, out);
            new RgbToBgr().transform(out, out);
            src = out;
        }
        BufferedImage dst = new BufferedImage(src.getCroppedWidth(), src.getCroppedHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        if (src.getCrop() == null)
            toBufferedImage8Bit2(src, dst);
        else
            toBufferedImageCropped8Bit(src, dst);
        return dst;
    }

    // this method is from Jcodec AWTUtils.java.
    private void toBufferedImage8Bit2(Picture src, BufferedImage dst) {
        byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
        byte[] srcData = src.getPlaneData(0);
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (srcData[i] + 128);
        }
    }

    // this method is from Jcodec AWTUtils.java.
    private static void toBufferedImageCropped8Bit(Picture src, BufferedImage dst) {
        byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
        byte[] srcData = src.getPlaneData(0);
        int dstStride = dst.getWidth() * 3;
        int srcStride = src.getWidth() * 3;
        for (int line = 0, srcOff = 0, dstOff = 0; line < dst.getHeight(); line++) {
            for (int id = dstOff, is = srcOff; id < dstOff + dstStride; id += 3, is += 3) {
                data[id] = (byte) (srcData[is] + 128);
                data[id + 1] = (byte) (srcData[is + 1] + 128);
                data[id + 2] = (byte) (srcData[is + 2] + 128);
            }
            srcOff += srcStride;
            dstOff += dstStride;
        }
    }

    /*
     * For type PNG - first frame given in int[] is taken by default
     */
    public static String create(String filePath, Type type, int[] framesForThumbnail) {
        ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();
        File file = Paths.get(filePath).toFile();
        long startTime = new Date().getTime();
        try {
            log.debug("Creating thumbnail for file: " + file.getAbsolutePath());
            if (!(framesForThumbnail.length > 0)) {
                log.error("framesForThumbnail length invalid. Provide at least one frame position");
                return "";
            }
            File imageFile = thumbnailGenerator.createThumbnailFromVideo(file, type, framesForThumbnail);
            if (imageFile != null) {
                long timeTaken = new Date().getTime() - startTime;
                System.out.println("Created thumbnail as: " + imageFile.getAbsolutePath() + " in millis: " + timeTaken);
                return imageFile.getAbsolutePath();
            }
        } catch (IOException | JCodecException e) {
            log.error("Error occurred while extracting image for thumbnail: " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception occurred while extracting image for thumbnail: " + e.getMessage());
        }
        return "";
    }

    public static enum Type {
        PNG,
        GIF
    }
}
