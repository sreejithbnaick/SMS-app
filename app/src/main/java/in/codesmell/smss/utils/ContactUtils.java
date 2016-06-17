package in.codesmell.smss.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

/**
 * Created by Sreejith on 17/6/16.
 */
public class ContactUtils {
    public static boolean hasContactPermission(Context context) {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }
}
