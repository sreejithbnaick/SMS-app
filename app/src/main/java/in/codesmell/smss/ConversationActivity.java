package in.codesmell.smss;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.codesmell.smss.data.ContactData;
import in.codesmell.smss.data.ContactLoaderCallbacks;
import in.codesmell.smss.data.ContactResult;
import in.codesmell.smss.data.ConversationAdapter;
import in.codesmell.smss.data.SmsData;
import in.codesmell.smss.data.SmsLoaderCallbacks;
import in.codesmell.smss.data.SmsResult;
import in.codesmell.smss.db.DBConstants;
import in.codesmell.smss.utils.ResultListener;
import in.codesmell.smss.utils.SmsUtils;
import in.codesmell.smss.utils.UiUtils;

public class ConversationActivity extends AppCompatActivity {
    private static final int SMS_LOADER_ID = 2;
    private static final int CONTACT_LOADER_ID = 3;
    public static final String BUNDLE_ADDRESS = "address";
    public static final String BUNDLE_REVERSE_ADDRESS = "raddress";
    public static final String BUNDLE_SHOW_KEYBOARD = "showkeyboard";
    public static final String BUNDLE_SCROLL_ID = "scrollId";
    public RecyclerView recyclerView;
    public ConversationAdapter adapter;
    private String address, reverseAddress;
    private int scrollId = -1;
    private Handler handler = new Handler();
    private EditText input;
    private Button send;
    private boolean contactLoaded = false;

    public static Intent createIntent(Context context, String address, String reverseAddress, boolean showKeyboard, int scrollId) {
        Intent intent = new Intent(context, ConversationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ConversationActivity.BUNDLE_ADDRESS, address);
        if (reverseAddress != null)
            bundle.putString(ConversationActivity.BUNDLE_REVERSE_ADDRESS, reverseAddress);
        bundle.putBoolean(BUNDLE_SHOW_KEYBOARD, showKeyboard);
        bundle.putInt(BUNDLE_SCROLL_ID, scrollId);
        intent.putExtras(bundle);
        return intent;
    }

    public static Intent createIntent(Context context, String address) {
        return createIntent(context, address, null, false, -1);
    }

    public static Intent createIntent(Context context, String address, boolean showKeyboard) {
        return createIntent(context, address, null, showKeyboard, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            address = bundle.getString(BUNDLE_ADDRESS);
            reverseAddress = bundle.getString(BUNDLE_REVERSE_ADDRESS);
            scrollId = bundle.getInt(BUNDLE_SCROLL_ID, -1);
            if (TextUtils.isEmpty(address)) {
                finish();
            } else {
                if (TextUtils.isEmpty(reverseAddress))
                    reverseAddress = SmsUtils.getReverseAddress(address);

                setContentView(R.layout.activity_conversation);
                setTitle(address);
                try {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

                LinearLayoutManager lm = new LinearLayoutManager(this);
                lm.setStackFromEnd(true);
                lm.setReverseLayout(true);

                recyclerView.setLayoutManager(lm);
                adapter = new ConversationAdapter(this);
                getLoaderManager().initLoader(CONTACT_LOADER_ID, null, new ContactLoaderCallbacks(this, new ResultListener<ContactResult>() {
                    @Override
                    public void onResult(ContactResult result) {
                        contactLoaded = true;
                        String contact = address;
                        ArrayList<ContactData> contactList = result.contactMap.get(reverseAddress);
                        if (contactList!=null && contactList.size() > 0) {
                            String name = contactList.get(0).name;
                            if (!TextUtils.isEmpty(name))
                                contact = name;
                        }
                        setTitle(contact);
                        getLoaderManager().initLoader(SMS_LOADER_ID, null, new SmsLoaderCallbacks(ConversationActivity.this, loaderResult)).forceLoad();
                    }
                })).forceLoad();

                recyclerView.setAdapter(adapter);

                input = (EditText) findViewById(R.id.input);
                send = (Button) findViewById(R.id.send);

                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = input.getText().toString();
                        input.setText("");
                        if (!TextUtils.isEmpty(text)) {
                            SmsUtils.sendSMS(ConversationActivity.this, address, text);
                            Toast.makeText(ConversationActivity.this,"Sending message",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                boolean showKeyboard = bundle.getBoolean(BUNDLE_SHOW_KEYBOARD, false);
                if (showKeyboard) {
                    UiUtils.showKeyboard(input, true);
                }
            }
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver().unregisterContentObserver(contentObserver);
        getContentResolver().registerContentObserver(DBConstants.AndroidDB.CONTENT_URI, true, contentObserver);
    }

    @Override
    protected void onStop() {
        try {
            getContentResolver().unregisterContentObserver(contentObserver);
            getLoaderManager().destroyLoader(SMS_LOADER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    ContentObserver contentObserver = new ContentObserver(handler) {
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("COSER", "ConversationActivity: onChange");
            if (contactLoaded)
                getLoaderManager().restartLoader(SMS_LOADER_ID, null, new SmsLoaderCallbacks(ConversationActivity.this, loaderResult)).forceLoad();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }
    };

    ResultListener<SmsResult> loaderResult = new ResultListener<SmsResult>() {
        @Override
        public void onResult(SmsResult result) {
            if (adapter != null) {
                int index = 0;
                ArrayList<SmsData> datas = result.groupDataMap.get(reverseAddress);
                if (datas != null)
                    index = performReadUpdate(datas);
                adapter.setData(address, datas);
                recyclerView.scrollToPosition(scrollId != -1 ? index : 0);

            }
        }
    };

    private int performReadUpdate(List<SmsData> datas) {
        int index = 0, i = 0;
        final ArrayList<Integer> unreadIds = new ArrayList<>();
        for (SmsData data : datas) {
            if (!data.read)
                unreadIds.add(data.id);
            if (data.id == scrollId)
                index = i;
            i++;
        }
        if (unreadIds.size() > 0) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    SmsUtils.batchUpdateRead(ConversationActivity.this, unreadIds);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);

        }
        return index;
    }
}
