package kr.co.captv.pooq.checker;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wbqd on 15. 8. 19..
 */
public class Memory {
    public static final long BYTES_TO_KB = 1024;
    public static final long BYTES_TO_MB = BYTES_TO_KB * 1024;
    public static final long BYTES_TO_GB = BYTES_TO_MB * 1024;
    public static final long BYTES_TO_TB = BYTES_TO_GB * 1024;

    public volatile String value = "0";
    public volatile String keys;

    public static Map<String, String> parseRam(final String procMem) {
        final LinkedHashMap<String, String> ramMap = new LinkedHashMap<String, String>();

        final String lines[] = procMem.trim().split("\n");

        for (String line : lines) {
            final String[] token = line.split(" ");
            // proc mem output looks like this each line: "info         byte kB" with tons of non-utf8-spaces in between,
            // so split(" ") doesn't work properly, but we can just take the first and length-2 token index to get what we want
            ramMap.put(token[0], formatBytes(Integer.valueOf(token[token.length - 2])));
        }

        return ramMap;
    }

    // alternatve to Formatter.formatFileSize which doesn't show bytes and rounds to int
    public static String formatBytes(final long bytes) {
        if (bytes <= 0)
            return "0 bytes";

        return bytes / BYTES_TO_TB > 0 ? String.format("%.2f TB", bytes / (float) BYTES_TO_TB) :
                bytes / BYTES_TO_GB > 0 ? String.format("%.2f GB", bytes / (float) BYTES_TO_GB) :
                        bytes / BYTES_TO_MB > 0 ? String.format("%.2f MB", bytes / (float) BYTES_TO_MB) :
                                bytes / BYTES_TO_KB > 0 ? String.format("%.2f KB", bytes / (float) BYTES_TO_KB) : bytes + " bytes";
    }

    public void setMap(final Map<String, String> map) {
        final StringBuffer keyBuffer = new StringBuffer();
        final StringBuffer valueBuffer = new StringBuffer();

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            keyBuffer.append(entry.getKey()).append("\n");
            valueBuffer.append(entry.getValue()).append("\n");
        }

        keys = keyBuffer.toString();
        value = valueBuffer.toString();
    }

    public static String getContentRandomAccessFile(String file) {

        final StringBuffer buffer = new StringBuffer();

        try {
            final RandomAccessFile reader = new RandomAccessFile(file, "r");
            String load = reader.readLine();
            while (load != null) {
//                Logger.v(reader.readLine());
                buffer.append(load).append("\n");
                load = reader.readLine();
            }
        } catch (final IOException e) {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            e.printStackTrace();
        }

        return buffer.toString();
    }
}
