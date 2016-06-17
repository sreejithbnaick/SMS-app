package in.codesmell.smss;

import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;

import in.codesmell.smss.utils.BackupHelper;


public class BackupActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private final int RESOLVE_CONNECTION_REQUEST_CODE = 100;
    private final int UPLOAD_REQUEST_CODE = 101;
    private Button retryButton;
    private View progress;
    private String uploadFile = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        retryButton = (Button) findViewById(R.id.button);
        progress = findViewById(R.id.progress);

        setTitle(getString(R.string.backup));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                if (!mGoogleApiClient.isConnected())
                    mGoogleApiClient.connect();
                else
                    onConnected(null);
            }
        });
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
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return BackupHelper.dbToJson(BackupActivity.this);
            }

            @Override
            protected void onPostExecute(final String data) {
                if (TextUtils.isEmpty(data)) {
                    Toast.makeText(BackupActivity.this, "No messages found to backup", Toast.LENGTH_SHORT).show();
                    retryButton.setVisibility(View.VISIBLE);
                } else {
                    Drive.DriveApi.newDriveContents(mGoogleApiClient)
                            .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

                                @Override
                                public void onResult(DriveApi.DriveContentsResult result) {

                                    if (!result.getStatus().isSuccess()) {
                                        Toast.makeText(BackupActivity.this, "Backup Failed", Toast.LENGTH_SHORT).show();
                                        retryButton.setVisibility(View.VISIBLE);
                                        return;
                                    }

                                    OutputStream outputStream = result.getDriveContents().getOutputStream();
                                    try {
                                        outputStream.write(data.getBytes());
                                    } catch (IOException e1) {
                                        Toast.makeText(BackupActivity.this, "Unable to write file contents.", Toast.LENGTH_SHORT).show();
                                        retryButton.setVisibility(View.VISIBLE);
                                        return;
                                    }

                                    uploadFile = "sms-backup-" + System.currentTimeMillis();
                                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                            .setMimeType("application/json").setTitle(uploadFile).build();
                                    // Create an intent for the file chooser, and start it.
                                    IntentSender intentSender = Drive.DriveApi
                                            .newCreateFileActivityBuilder()
                                            .setInitialMetadata(metadataChangeSet)
                                            .setInitialDriveContents(result.getDriveContents())
                                            .build(mGoogleApiClient);
                                    try {
                                        startIntentSenderForResult(
                                                intentSender, UPLOAD_REQUEST_CODE, null, 0, 0, 0);
                                    } catch (IntentSender.SendIntentException e) {
                                        Toast.makeText(BackupActivity.this, "Data upload failed", Toast.LENGTH_SHORT).show();
                                        retryButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                retryButton.setVisibility(View.VISIBLE);
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    retryButton.setVisibility(View.VISIBLE);
                }
                break;
            case UPLOAD_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(BackupActivity.this, "Backup upload successfully added to queue", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BackupActivity.this, "Backup Upload Failed/Canceled", Toast.LENGTH_SHORT).show();
                }
                retryButton.setVisibility(View.VISIBLE);
        }
    }

}