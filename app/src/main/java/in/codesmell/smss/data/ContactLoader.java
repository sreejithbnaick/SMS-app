package in.codesmell.smss.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import java.util.ArrayList;

import in.codesmell.smss.utils.ContactUtils;
import in.codesmell.smss.utils.SmsUtils;

public class ContactLoader extends AsyncTaskLoader<ContactResult> {
    private static final int INDEX_ID = 0;
    private static final int INDEX_NAME = 1;
    private static final int INDEX_CONTACT_ID = 2;
    private static final int INDEX_LOOKUP_KEY = 3;
    private static final int INDEX_NUMBER = 4;

    public ContactLoader(Context context, Bundle args) {
        super(context);
    }

    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
    };


    @Override
    public ContactResult loadInBackground() {
        ContactResult contactResult = new ContactResult();
        if (!ContactUtils.hasContactPermission(getContext()))
            return contactResult;

        Cursor cur;
        cur = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);

        if (cur == null)
            return contactResult;

        while (cur.moveToNext()) {
            int id = cur.getInt(INDEX_ID);
            String name = cur.getString(INDEX_NAME);
            int contact_id = cur.getInt(INDEX_CONTACT_ID);
            String lookupKey = cur.getString(INDEX_LOOKUP_KEY);
            String number = cur.getString(INDEX_NUMBER);

            String reverseAddress = SmsUtils.getReverseAddress(number.toLowerCase());
            ContactData data = new ContactData();
            data.id = id;
            data.contactId = contact_id;
            data.reverseAddress = reverseAddress;
            data.address = number;
            data.lookupKey = lookupKey;
            data.name=name;

            ArrayList<ContactData> list = contactResult.contactMap.get(reverseAddress);
            if (list == null) {
                contactResult.contacts.add(data);
                list = new ArrayList<>();
                contactResult.contactMap.put(reverseAddress, list);
            }
            list.add(data);
        }

        cur.close();
        return contactResult;
    }
}