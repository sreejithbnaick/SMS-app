package in.codesmell.smss.data;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

import in.codesmell.smss.ConversationActivity;
import in.codesmell.smss.R;

public class SMSReceiver extends BroadcastReceiver {
    private final String DEBUG_TAG = getClass().getSimpleName().toString();
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private Context context;
    private Intent mIntent;

    // Retrieve SMS
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Broadcast received: " + intent.getAction(), Toast.LENGTH_SHORT).show();
        mIntent = intent;

        String action = intent.getAction();

        if (action.equals(ACTION_SMS_RECEIVED)) {

            String address = "Unknown", str = "";

            SmsMessage[] msgs = getMessagesFromIntent(mIntent);
            if (msgs != null) {
                for (int i = 0; i < msgs.length; i++) {
                    address = msgs[i].getOriginatingAddress();
                    str += msgs[i].getMessageBody().toString();
                    str += "\n";
                }
            }

            showNotification(context, address, str);
        }

    }

    public static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }

    protected void showNotification(Context context, String address, String message) {
        Intent intent = ConversationActivity.createIntent(context, address);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ConversationActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context).setContentTitle(address).setContentText(message).setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}