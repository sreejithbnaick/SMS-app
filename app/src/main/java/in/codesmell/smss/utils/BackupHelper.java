package in.codesmell.smss.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import in.codesmell.smss.db.DBConstants;

/**
 * Created by Sreejith on 17/6/16.
 */
public class BackupHelper {
    public static String dbToJson(Context context) {
        Cursor cursor = context.getContentResolver().query(DBConstants.AndroidDB.CONTENT_URI, null, null, null, null);
        JSONArray resultSet = new JSONArray();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            //Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        //Log.d("TAG_NAME", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("TAG_NAME", resultSet.toString());
        return resultSet.toString();
    }

    public static void test(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data = dbToJson(context);
                try {
                    FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory()+"/sms-backup.json");
                    writer.write(data);
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void writeToFile(Context context, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
