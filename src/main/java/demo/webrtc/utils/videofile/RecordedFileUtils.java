package demo.webrtc.utils.videofile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;

/**
 * Created by kantipud on 28-12-2017.
 */
public class RecordedFileUtils {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();
    public static final String RECORD_FILE_PREFIX = "file://";

    public static long getFileSize(File file) {
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    public static boolean isValidFileWithSize(File file) {
        if (getFileSize(file) > 0) {
            return true;
        }
        return false;
    }

    public static boolean isValidFileWithSize(String file) {
        return isValidFileWithSize(new File(removePrefixInFileNameIfExists(file)));
    }

    public static String removePrefixInFileNameIfExists(String fileName) {
        if (StringUtils.startsWith(fileName, RECORD_FILE_PREFIX)) {
            return StringUtils.removeStart(fileName, RECORD_FILE_PREFIX);
        }
        return fileName;
    }

    public static void removeFileIfInvalidLength(String filePath) {
        File file = new File(removePrefixInFileNameIfExists(filePath));
        if (file.exists() && !isValidFileWithSize(file)) {
            log.error("Deleting file as its invalid with length 0 " + file.getAbsolutePath());
            FileUtils.deleteQuietly(file);
        }
    }

    public static String createThumbnailIfValidRecording(String filePath) {
        if (isValidFileWithSize(filePath)) {
            return ThumbnailGenerator.create(removePrefixInFileNameIfExists(filePath), ThumbnailGenerator.Type.GIF, new int[]{2, 10, 15});
        }
        return "";
    }
}
