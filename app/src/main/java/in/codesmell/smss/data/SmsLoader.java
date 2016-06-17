package in.codesmell.smss.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;

import in.codesmell.smss.db.DBConstants.AndroidDB;
import in.codesmell.smss.utils.SmsUtils;

public class SmsLoader extends AsyncTaskLoader<SmsResult> {
    private static final int TYPE_HOME = 0;
    private static final int TYPE_SEARCH = 1;
    public static final String BUNDLE_SEARCH_TEXT = "searchText";
    public static final String BUNDLE_TYPE = "type";

    private int type = 0;
    private String searchText = "";

    public SmsLoader(Context context, Bundle args) {
        super(context);
        if (args != null) {
            searchText = args.getString(BUNDLE_SEARCH_TEXT);
            if (TextUtils.isEmpty(searchText))
                type = TYPE_HOME;
            else
                type = TYPE_SEARCH;
        }
    }

    @Override
    public SmsResult loadInBackground() {
        SmsResult smsResult = new SmsResult();
        if (!SmsUtils.hasSmsPermission(getContext()))
            return smsResult;

        Cursor cur;
        if (type == TYPE_HOME)
            cur = getContext().getContentResolver().query(AndroidDB.CONTENT_URI, AndroidDB.PROJECTION, null, null, AndroidDB.DATE + " DESC");
        else
            cur = getContext().getContentResolver().query(AndroidDB.CONTENT_URI, AndroidDB.PROJECTION, AndroidDB.BODY + " like ? ", new String[]{"%" + searchText + "%"}, AndroidDB.DATE + " DESC");


        if (cur == null)
            return smsResult;
        while (cur.moveToNext()) {
            int id = cur.getInt(AndroidDB.INDEX_ID);
            String address = cur.getString(AndroidDB.INDEX_ADDRESS);
            String body = cur.getString(AndroidDB.INDEX_BODY);
            long date = cur.getLong(AndroidDB.INDEX_DATE);
            boolean read = cur.getInt(AndroidDB.INDEX_READ) != 0;
            int type = cur.getInt(AndroidDB.INDEX_TYPE);
            if (TextUtils.isEmpty(address))
                continue;
            String reverseAddress = SmsUtils.getReverseAddress(address.toLowerCase());

            SmsData data = new SmsData();
            data.address = address;
            data.body = body;
            data.reverseAddress = reverseAddress;
            data.date = date;
            data.read = read;
            data.type = type;
            data.id = id;

            ArrayList<SmsData> list = smsResult.groupDataMap.get(reverseAddress);
            if (list == null) {
                smsResult.groupData.add(data);
                list = new ArrayList<>();
                smsResult.groupDataMap.put(reverseAddress, list);
            }
            list.add(data);
        }
        cur.close();
        return smsResult;
    }
}