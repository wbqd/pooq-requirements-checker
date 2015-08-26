package kr.co.captv.pooq.checker;

import android.app.ActivityManager;
import android.content.Context;

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

    public static long getTotalRam() {
        Map<String, Integer> ramMap = parseRam(getContentRandomAccessFile("proc/meminfo"));
        return ramMap.get("MemTotal:") * 1024;
    }

    public static long getFreeRam() {
        Map<String, Integer> ramMap = parseRam(getContentRandomAccessFile("proc/meminfo"));
        return ramMap.get("MemFree:") * 1024;
    }

    public static long getAvailRam(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }
}
