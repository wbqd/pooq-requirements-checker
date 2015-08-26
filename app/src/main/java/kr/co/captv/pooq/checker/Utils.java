package kr.co.captv.pooq.checker;

/**
 * Created by wbqd on 15. 8. 26..
 */
public class Utils {

    public static final long BYTES_TO_KB = 1024;
    public static final long BYTES_TO_MB = BYTES_TO_KB * 1024;
    public static final long BYTES_TO_GB = BYTES_TO_MB * 1024;
    public static final long BYTES_TO_TB = BYTES_TO_GB * 1024;

    // Format numbers into percent style.
    public static String formatPercent(final float usage) {
        return String.format("%.2f %s", usage, "%");
    }

    // Auto convert bytes into formated byted with proper unit.
    public static String formatBytes(final long bytes) {
        if (bytes <= 0)
            return "0 bytes";

        return bytes / BYTES_TO_GB > 0 ? String.format("%.2f GB", bytes / (float) BYTES_TO_GB) :
                        bytes / BYTES_TO_MB > 0 ? String.format("%.2f MB", bytes / (float) BYTES_TO_MB) :
                                bytes / BYTES_TO_KB > 0 ? String.format("%.2f KB", bytes / (float) BYTES_TO_KB) : bytes + " bytes";
    }
}
