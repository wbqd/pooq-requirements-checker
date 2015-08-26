package kr.co.captv.pooq.checker;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wbqd on 15. 8. 19..
 *
 * This class parse RAM information from "/proc/meminfo" and manipulate it in proper way.
 */
public class Ram {
    public static final long BYTES_TO_KB = 1024;
    public static final long BYTES_TO_MB = BYTES_TO_KB * 1024;
    public static final long BYTES_TO_GB = BYTES_TO_MB * 1024;
    public static final long BYTES_TO_TB = BYTES_TO_GB * 1024;

    public static Map<String, Integer> parseRam(final String procMem) {
        final LinkedHashMap<String, Integer> ramMap = new LinkedHashMap<>();

        final String lines[] = procMem.trim().split("\n");

        for (String line : lines) {
            final String[] token = line.split(" ");
//            ramMap.put(token[0], formatBytes(Integer.valueOf(token[token.length - 2])));
            ramMap.put(token[0], Integer.parseInt(token[token.length - 2]));
        }

        return ramMap;
    }

    public static String formatBytes(final int kiloBytes) {
        int bytes = kiloBytes * (int)BYTES_TO_KB;
        if (bytes <= 0)
            return "0 bytes";

        return bytes / BYTES_TO_TB > 0 ? String.format("%.2f TB", bytes / (float) BYTES_TO_TB) :
                bytes / BYTES_TO_GB > 0 ? String.format("%.2f GB", bytes / (float) BYTES_TO_GB) :
                        bytes / BYTES_TO_MB > 0 ? String.format("%.2f MB", bytes / (float) BYTES_TO_MB) :
                                bytes / BYTES_TO_KB > 0 ? String.format("%.2f KB", bytes / (float) BYTES_TO_KB) : bytes + " bytes";
    }

    public static String getContentRandomAccessFile(String file) {

        final StringBuffer buffer = new StringBuffer();

        try {
            final RandomAccessFile reader = new RandomAccessFile(file, "r");
            int i = 0;
            String load = reader.readLine();
            while (load != null && i++ < 2) {
                buffer.append(load).append("\n");
                load = reader.readLine();
            }
            reader.close();
        } catch (final IOException e) {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static int getTotalRam() {
        Map<String, Integer> ramMap = parseRam(getContentRandomAccessFile("proc/meminfo"));
        return ramMap.get("MemTotal:");
    }

    public static int getFreeRam() {
        Map<String, Integer> ramMap = parseRam(getContentRandomAccessFile("proc/meminfo"));
        return ramMap.get("MemFree:");
    }
}
