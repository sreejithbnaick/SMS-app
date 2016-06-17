package in.codesmell.smss.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import in.codesmell.smss.db.DBConstants;

/**
 * Created by Sreejith on 16/6/16.
 */
public class SmsUtils {
    private static String SENT = "SMS_SENT";
    private static String DELIVERED = "SMS_DELIVERED";
    private static int MAX_SMS_MESSAGE_LENGTH = 160;

    public static void sendSMS(Context context, String phoneNumber, String message) {
        PendingIntent piSent = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);
        SmsManager smsManager = SmsManager.getDefault();

        int length = message.length();
        if (length > MAX_SMS_MESSAGE_LENGTH) {
            ArrayList<String> messagelist = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, messagelist, null, null);
        } else
            smsManager.sendTextMessage(phoneNumber, null, message, piSent, piDelivered);
    }

    public static String getReverseAddress(String phoneNumber) {
        String reverseAddress = PhoneNumberUtils.toCallerIDMinMatch(phoneNumber.toLowerCase());
        if (TextUtils.isEmpty(reverseAddress) || !StringUtils.isInteger(reverseAddress)) {
            reverseAddress = phoneNumber;
        }
        return reverseAddress;
    }

    public static int updateRead(Context context, int id) {
        ContentValues values = new ContentValues();
        values.put("read", true);
        return context.getContentResolver().update(Uri.parse("content://sms/"), values, "_id=" + id, null);
    }

    public static void batchUpdateRead(Context context, List<Integer> ids) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (Integer id : ids) {
            ops.add(ContentProviderOperation.newUpdate(DBConstants.AndroidDB.CONTENT_URI)
                    .withSelection(DBConstants.AndroidDB._ID + " = ? ", new String[]{String.valueOf(id)})
                    .withValue(DBConstants.AndroidDB.READ, String.valueOf(1))
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(DBConstants.AndroidDB.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasSmsPermission(Context context) {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void setDefaultSmsApp(Activity activity) {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                activity.getPackageName());
        activity.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isDefaultSmsApp(Context context) {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2 || Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName());
    }
}
