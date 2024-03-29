package org.acra.util;

import java.io.DataOutputStream;
import java.io.File;

import org.acra.ACRA;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;

/**
 * Responsible for providing base utilities used when constructing the report.
 * <p/>
 * @author William Ferguson
 * @since 4.3.0
 */
public final class ReportUtils {

    /**
     * Calculates the free memory of the device. This is based on an inspection of the filesystem, which in android
     * devices is stored in RAM.
     *
     * @return Number of bytes available.
     */
    public static long getAvailableInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize = stat.getBlockSize();
        final long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * Calculates the total memory of the device. This is based on an inspection of the filesystem, which in android
     * devices is stored in RAM.
     *
     * @return Total number of bytes.
     */
    public static long getTotalInternalMemorySize() {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize = stat.getBlockSize();
        final long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * Returns the DeviceId according to the TelephonyManager.
     *
     * @param context   Context for the application being reported.
     * @return Returns the DeviceId according to the TelephonyManager or null if there is no TelephonyManager.
     */
    public static String getDeviceId(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (RuntimeException e) {
            Log.w(ACRA.LOG_TAG, "Couldn't retrieve DeviceId for : " + context.getPackageName(), e);
            return null;
        }
    }

    public static String getApplicationFilePath(Context context) {
        final File filesDir = context.getFilesDir();
        if (filesDir != null) {
            return filesDir.getAbsolutePath();
        }

        Log.w(ACRA.LOG_TAG, "Couldn't retrieve ApplicationFilePath for : " + context.getPackageName());
        return "Couldn't retrieve ApplicationFilePath";
    }

    /**
     * Returns a String representation of the content of a {@link android.view.Display} object.
     *
     * @param context   Context for the application being reported.
     * @return A String representation of the content of the default Display of the Window Service.
     */
    public static String getDisplayDetails(Context context) {
        try {
            final WindowManager windowManager = (WindowManager) context.getSystemService(android.content.Context.WINDOW_SERVICE);
            final Display display = windowManager.getDefaultDisplay();
            final DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);

            final StringBuilder result = new StringBuilder();
            result.append("width=").                    append(display.getWidth()).append('\n');
            result.append("height=").                   append(display.getHeight()).append('\n');
            result.append("pixelFormat=").              append(display.getPixelFormat()).append('\n');
            result.append("refreshRate=").              append(display.getRefreshRate()).append("fps").append('\n');
            result.append("metrics.density=x").         append(metrics.density).append('\n');
            result.append("metrics.scaledDensity=x").   append(metrics.scaledDensity).append('\n');
            result.append("metrics.widthPixels=").      append(metrics.widthPixels).append('\n');
            result.append("metrics.heightPixels=").     append(metrics.heightPixels).append('\n');
            result.append("metrics.xdpi=").             append(metrics.xdpi).append('\n');
            result.append("metrics.ydpi=").             append(metrics.ydpi);
            return result.toString();

        } catch (RuntimeException e) {
            Log.w(ACRA.LOG_TAG, "Couldn't retrieve DisplayDetails for : " + context.getPackageName(), e);
            return "Couldn't retrieve Display Details";
        }
    }
    
    /**
     * Utility method used for debugging purposes, writes the content of a SparseArray to a String.
     * @param sparseArray
     * @return "{ key1 => value1, key2 => value2, ...}"
     */
    public static String sparseArrayToString(SparseArray<?> sparseArray) {
        StringBuilder result = new StringBuilder();
        if (sparseArray == null) {
            return "null";
        }

        result.append('{');
        for (int i = 0; i < sparseArray.size(); i++) {
            result.append(sparseArray.keyAt(i));
            result.append(" => ");
            if (sparseArray.valueAt(i) == null) {
                result.append("null");
            } else {
                result.append(sparseArray.valueAt(i).toString());
            }
            if(i < sparseArray.size() - 1) {
                result.append(", ");
            }
        }
        result.append('}');
        return result.toString();
    }
    
    
    ///取得root权限的方法
    public boolean RootCmd(String cmd) {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}
}
