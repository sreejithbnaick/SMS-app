package in.codesmell.smss.db;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;

import static android.provider.Telephony.Sms;

/**
 * Created by sreejith on 15/6/16.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public interface DBConstants {
    String PARAMETER_NOTIFY = "notify";
    String PARAMETER_CUSTOMQUERY = "custom";

    interface AndroidDB {
        String AUTHORITY = "sms";
        boolean flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        Uri CONTENT_URI = flag ? Sms.CONTENT_URI : Uri.parse("content://sms");

        String _ID = flag ? Sms._ID : "_id";
        String THREAD_ID = flag ? Sms.THREAD_ID : "thread_id";
        String ADDRESS = flag ? Sms.ADDRESS : "address";
        String BODY = flag ? Sms.BODY : "body";
        String DATE = flag ? Sms.DATE : "date";
        String READ = flag ? Sms.READ : "read";
        String TYPE = flag ? Sms.TYPE : "type";

        String[] PROJECTION = new String[]{_ID,
                ADDRESS, DATE, TYPE, BODY, THREAD_ID, READ};

        int INDEX_ID = 0;
        int INDEX_ADDRESS = 1;
        int INDEX_DATE = 2;
        int INDEX_TYPE = 3;
        int INDEX_BODY = 4;
        int INDEX_THREAD_ID = 5;
        int INDEX_READ = 6;
    }

    interface SMS extends AndroidDB {
        String TABLE_NAME = "sms";
        String REVERSE_ID = "numid";
        String STATUS = "status";

        Uri URI = Uri.parse("content://" +
                SmsProvider.AUTHORITY + "/" +
                TABLE_NAME);

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                ADDRESS + "TEXT, " +
                DATE + " INTEGER, " +
                READ + " INTEGER DEFAULT 0, " +
                STATUS + " INTEGER DEFAULT -1, " +
                TYPE + " INTEGER, " +
                BODY + " TEXT, " +
                REVERSE_ID + " TEXT " +
                ")";

    }
}
