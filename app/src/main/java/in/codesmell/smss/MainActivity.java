package in.codesmell.smss;

import android.Manifest;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import in.codesmell.smss.data.ContactLoaderCallbacks;
import in.codesmell.smss.data.ContactResult;
import in.codesmell.smss.data.SmsAdapter;
import in.codesmell.smss.data.SmsData;
import in.codesmell.smss.data.SmsLoader;
import in.codesmell.smss.data.SmsLoaderCallbacks;
import in.codesmell.smss.data.SmsResult;
import in.codesmell.smss.db.DBConstants;
import in.codesmell.smss.utils.ContactUtils;
import in.codesmell.smss.utils.ResultListener;
import in.codesmell.smss.utils.SmsUtils;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_LOADER_ID = 1;
    private static final int CONTACT_LOADER_ID = 2;
    RecyclerView recyclerView;
    SmsAdapter smsAdapter;
    Handler handler = new Handler();
    private boolean isPaused, needRefresh, hasSmsPermission = false, isDefaultSMSApp = false, contactLoaded = false, hasContactPermission = false;
    private String searchText;
    private View permissionLayout;
    private Button permissionButton;
    private TextView permissionText;
    private MenuItem defaultSMSMenuItem;
    public ContactResult contactResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionLayout = findViewById(R.id.permission_layout);
        permissionButton = (Button) findViewById(R.id.button);
        permissionText = (TextView) findViewById(R.id.text);

        hasSmsPermission = SmsUtils.hasSmsPermission(this);
        hasContactPermission = ContactUtils.hasContactPermission(this);
        if (!hasContactPermission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                showContactPermissionDialog();
                permissionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                100);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        100);
            }
        } else if (!hasSmsPermission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_SMS)) {
                permissionText.setText(R.string.sms_permission_is_requirec_to_run_this_application);
                permissionButton.setText(R.string.get);

                permissionLayout.setVisibility(View.VISIBLE);
                permissionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_SMS},
                                100);
                    }
                });

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_SMS},
                        100);
            }
        }


        smsAdapter = new SmsAdapter(this, onClickListener);
        recyclerView.setAdapter(smsAdapter);

        isDefaultSMSApp = SmsUtils.isDefaultSmsApp(this);
        if (hasSmsPermission && !isDefaultSMSApp) {
            showDefaultAppDialog();
        }
    }

    private void loadContactAndSms() {
        if (contactLoaded)
            return;
        getLoaderManager().initLoader(CONTACT_LOADER_ID, null, new ContactLoaderCallbacks(this, new ResultListener<ContactResult>() {
            @Override
            public void onResult(ContactResult result) {
                contactLoaded = true;
                contactResult = result;
                getLoaderManager().initLoader(SMS_LOADER_ID, null, new SmsLoaderCallbacks(MainActivity.this, loaderResult)).forceLoad();
            }
        })).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        defaultSMSMenuItem = menu.findItem(R.id.sms);
        defaultSMSMenuItem.setVisible(!isDefaultSMSApp);

        final MenuItem createMenu = menu.findItem(R.id.create);
        createMenu.getActionView().findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMenu.collapseActionView();
                String address = ((EditText) createMenu.getActionView().findViewById(R.id.editText)).getText().toString();
                if (!TextUtils.isEmpty(address))
                    startActivity(ConversationActivity.createIntent(MainActivity.this, address, true));
            }
        });

        final MenuItem searchMenu = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenu.getActionView();

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                Bundle bundle = new Bundle();
                bundle.putString(SmsLoader.BUNDLE_SEARCH_TEXT, searchText);
                if (contactLoaded)
                    getLoaderManager().restartLoader(SMS_LOADER_ID, bundle, new SmsLoaderCallbacks(MainActivity.this, loaderResult)).forceLoad();
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sms)
            SmsUtils.setDefaultSmsApp(this);
        else if (item.getItemId() == R.id.backup)
            startActivity(new Intent(this, BackupActivity.class));
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
            getLoaderManager().destroyLoader(SMS_LOADER_ID);
            getContentResolver().unregisterContentObserver(contentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean gotSMSPermission = SmsUtils.hasSmsPermission(this);
        boolean isDefaultSMSApp = SmsUtils.isDefaultSmsApp(this);
        boolean gotContactPermission = ContactUtils.hasContactPermission(this);
        if (needRefresh || hasSmsPermission != gotSMSPermission || isPaused) {
            isPaused = false;
            needRefresh = false;
            Bundle bundle = new Bundle();
            bundle.putString(SmsLoader.BUNDLE_SEARCH_TEXT, searchText);
            if (contactLoaded)
                getLoaderManager().restartLoader(SMS_LOADER_ID, bundle, new SmsLoaderCallbacks(MainActivity.this, loaderResult)).forceLoad();
        }
        if (!gotContactPermission) {
            showContactPermissionDialog();
        } else if (!gotSMSPermission) {
            showSMSPermissionDialog();
        } else {
            if (!isDefaultSMSApp)
                showDefaultAppDialog();
            else
                permissionLayout.setVisibility(View.GONE);
            loadContactAndSms();
        }

        hasSmsPermission = gotSMSPermission;
        hasContactPermission = gotContactPermission;
        if (defaultSMSMenuItem != null)
            defaultSMSMenuItem.setVisible(!isDefaultSMSApp);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    private void showDefaultAppDialog() {
        permissionText.setText(R.string.make_app_default_sms_app);
        permissionButton.setText(R.string.set);
        permissionLayout.setVisibility(View.VISIBLE);
        permissionButton.setOnClickListener(defaultAppClickListener);
    }

    private void showContactPermissionDialog() {
        permissionText.setText(R.string.contact_permission_is_requirec_to_run_this_application);
        permissionButton.setText(R.string.get);
        permissionLayout.setVisibility(View.VISIBLE);
        permissionButton.setOnClickListener(contactPermClickListener);
    }

    private void showSMSPermissionDialog() {
        permissionText.setText(R.string.sms_permission_is_requirec_to_run_this_application);
        permissionButton.setText(R.string.get);
        permissionLayout.setVisibility(View.VISIBLE);
        permissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_SMS},
                        100);
            }
        });
    }

    View.OnClickListener contactPermClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    100);
        }
    };

    View.OnClickListener defaultAppClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            permissionLayout.setVisibility(View.GONE);
            SmsUtils.setDefaultSmsApp(MainActivity.this);
        }
    };

    ResultListener<SmsResult> loaderResult = new ResultListener<SmsResult>() {
        @Override
        public void onResult(SmsResult result) {
            if (smsAdapter != null)
                smsAdapter.setData(result);
        }
    };

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SmsData data = (SmsData) v.getTag();
            if (data != null) {
                startActivity(ConversationActivity.createIntent(MainActivity.this, data.address, data.reverseAddress, false, data.id));
            }
        }
    };

    ContentObserver contentObserver = new ContentObserver(handler) {
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("COSER", "onChange");
            if (isPaused)
                needRefresh = true;
            else {
                Bundle bundle = new Bundle();
                bundle.putString(SmsLoader.BUNDLE_SEARCH_TEXT, searchText);
                if (contactLoaded)
                    getLoaderManager().restartLoader(SMS_LOADER_ID, bundle, new SmsLoaderCallbacks(MainActivity.this, loaderResult)).forceLoad();
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }
    };
}
