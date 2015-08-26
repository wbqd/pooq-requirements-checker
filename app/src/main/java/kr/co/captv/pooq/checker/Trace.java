package kr.co.captv.pooq.checker;

import android.os.Debug;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by wbqd on 15. 8. 26..
 */
public class Trace {
    public final static String TAG = "pooq_checker";
    public final static boolean DEBUG = Constants.SHOW_LOG;
    public final static boolean DEBUG_FILE = false;
    private final static int FORMAT_YYYYDDMM_HHMMSS = 0;
    private final static int FORMAT_YYYYDDMM = 1;
    private final static int FORMAT_HHMMSS = 2;

    public static void d(String msg) {
        if (DEBUG) {
            log('D', msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            log('E', msg);
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            log('W', msg);
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            log('I', msg);
        }
    }

    public static void v(String msg) {
        if (DEBUG) {
            log('V', msg);
        }
    }

    public static void e(Exception e) {
        if (DEBUG) {
            log(e);
        } else {
            e.printStackTrace();
        }
    }

    private static void log(char level, String msg) {
        if (DEBUG) {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            String fileName = thread.getStackTrace()[4].getFileName();
            int nLine = thread.getStackTrace()[4].getLineNumber();

            if (fileName.length() > 20) {
                fileName = fileName.substring(0, 20);
            }

            String strLog = String.format("%s:%-10s[%-20s %5d]%s\n", TAG, threadName, fileName, nLine, msg);

            switch (level) {
                case 'D':
                    Log.d(TAG, strLog);
                    break;
                case 'E':
                    Log.e(TAG, strLog);
                    break;
                case 'W':
                    Log.w(TAG, strLog);
                    break;
                case 'I':
                    Log.i(TAG, strLog);
                    break;
                case 'V':
                    Log.v(TAG, strLog);
                    break;
            }
        }

    }

    public static void dump(byte[] buf) {
        if (DEBUG) {
            String s = "0123456789ABCDEF";
            byte[] digit = s.getBytes();

            StringBuilder strBuild = new StringBuilder();
            for (int i = 0; i < buf.length; i++) {

                int n = (buf[i] >> 4) & 0x0f;
                strBuild.append((char) digit[n]);
                n = buf[i] & 0x0f;
                strBuild.append((char) digit[n]);

                strBuild.append(' ');

                if (i == buf.length - 1) {
                    strBuild.append("\n");
                    Log.d(TAG, strBuild.toString());
                } else if (i != 0 && (i + 1) % 16 == 0) {
                    strBuild.append("\n");
                    Log.d(TAG, strBuild.toString());
                    strBuild = new StringBuilder();
                }
            }
        }
    }

    public static String getCurTime(int nFormat) {
        //        String strNow = "";
        Calendar calendar = Calendar.getInstance();
        // 1. year
        String strYear = "" + calendar.get(Calendar.YEAR);
        // 2. month
        String strMonth = "" + (calendar.get(Calendar.MONTH) + 1) + "";

        if (strMonth.length() < 2)
            strMonth = "0" + strMonth;

        // 3. date( day )
        String strDate = calendar.get(Calendar.DATE) + "";
        if (strDate.length() < 2)
            strDate = "0" + strDate;

        // 4. hour
        String strHour = calendar.get(Calendar.HOUR) + "";
        if (calendar.get(Calendar.AM_PM) == 1) {
            strHour = Integer.parseInt(strHour) + 12 + "";
        }

        if (strHour.length() < 2)
            strHour = "0" + strHour;
        String strMinute = calendar.get(Calendar.MINUTE) + "";

        // 5. minute
        if (strMinute.length() < 2)
            strMinute = "0" + strMinute;
        String strSecond = calendar.get(Calendar.SECOND) + "";

        // 6. second
        if (strSecond.length() < 2)
            strSecond = "0" + strSecond;

        // formatting
        switch (nFormat) {
            case FORMAT_YYYYDDMM: {
                return strYear + strMonth + strDate;
            }
            case FORMAT_HHMMSS: {
                return strHour + strMinute + strSecond;
            }
            default:
        }

        return strYear + strMonth + strDate + "_" + strHour + strMinute + strSecond;
    }

    public static void file(String log) {
        if (DEBUG_FILE) {
            File dir = new File("/sdcard/nooto/" + "/log");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file("/sdcard/nooto/" + "/log/log_" + getCurTime(FORMAT_YYYYDDMM)
                            + ".txt",
                    getCurTime(FORMAT_YYYYDDMM_HHMMSS)
                            + " " + log + "\r\n", true);
        }
    }

    private synchronized static void file(String path, String log, boolean bAppend) {
        if (DEBUG_FILE) {
            try {
                File file = new File(path);
                if (file.exists() && !bAppend) {
                    file.delete();
                }

                if (!file.exists()) {
                    boolean created = file.createNewFile();
                    if (!created) {
                        return;
                    }
                }

                if (log == null) {
                    Trace.d(" -- return buf is null");
                    return;
                }

                ParcelFileDescriptor parcel = ParcelFileDescriptor
                        .open(
                                file,
                                ParcelFileDescriptor.MODE_WORLD_READABLE
                                        | ParcelFileDescriptor.MODE_WORLD_WRITEABLE
                                        | ParcelFileDescriptor.MODE_READ_WRITE
                                        | ParcelFileDescriptor.MODE_CREATE
                                        | ParcelFileDescriptor.MODE_APPEND);

                FileOutputStream fos = new FileOutputStream(parcel.getFileDescriptor());
                fos.write(log.getBytes(), 0, log.getBytes().length);
                fos.flush();
                fos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                return;
            }
        }
    }

    private static synchronized void log(Exception e) {
        if (DEBUG) {
            StackTraceElement[] ele = e.getStackTrace();

            Thread t = Thread.currentThread();

            String strThreadName = t.getName();
            String strFileName = t.getStackTrace()[4].getFileName();
            int nLineNumber = t.getStackTrace()[4].getLineNumber();

            // limit filename length
            if (strFileName.length() > 20) {
                strFileName = strFileName.substring(0, 20);
            }

            int count = ele.length;
            String strLog = "";

            // print head line
            strLog = String.format("%-10s[%-20s:%5d] %s: %s", strThreadName, strFileName,
                    nLineNumber, e.getClass().getName(), e.getMessage());
            file(strLog);
            Log.e(TAG, strLog);

            // print stack trace
            for (int i = 0; i < count; i++) {
                strLog = String.format("%-10s[%-20s:%5d]    at %s %s (%s:%d)", strThreadName,
                        strFileName, nLineNumber, ele[i].getClassName(), ele[i].getMethodName(),
                        ele[i].getFileName(), ele[i].getLineNumber());

                file(strLog);
                Log.e(TAG, strLog);
            }
        }
    }

    public static void memory() {
        if (DEBUG) {
            float sep = 1024 * 1024f;
            log('E', "MEMINFO =================================================");
            log('E', "MEMINFO =================================================");
            log('E', "MEMINFO Native Heap size		= " + String.format("%.2fm", (float) (Debug.getNativeHeapSize() / sep)));
            log('E', "MEMINFO Native Heap Alloc 	= " + String.format("%.2fm", (float) (Debug.getNativeHeapAllocatedSize() / sep)));
            log('E', "MEMINFO Native Heap Freed 	= " + String.format("%.2fm", (float) (Debug.getNativeHeapFreeSize() / sep)));
            log('E', "MEMINFO =================================================");
            log('E', "MEMINFO Max Memory	  		= " + String.format("%.2fm", (float) (Runtime.getRuntime().maxMemory() / sep)));
            log('E', "MEMINFO Total Memory 			= " + String.format("%.2fm", (float) (Runtime.getRuntime().totalMemory() / sep)));
            log('E', "MEMINFO Free Memory 			= " + String.format("%.2fm", (float) (Runtime.getRuntime().freeMemory() / sep)));
            log('E', "MEMINFO =================================================");
        }
    }
}
