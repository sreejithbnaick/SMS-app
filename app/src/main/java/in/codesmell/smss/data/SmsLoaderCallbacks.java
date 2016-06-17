package in.codesmell.smss.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import in.codesmell.smss.utils.ResultListener;

public class SmsLoaderCallbacks implements LoaderManager.LoaderCallbacks<SmsResult> {
    private Context context;
    private ResultListener<SmsResult> resultListener;

    public SmsLoaderCallbacks(Context context, ResultListener<SmsResult> resultListener) {
        this.context = context;
        this.resultListener = resultListener;
    }

    @Override
    public Loader<SmsResult> onCreateLoader(int id, Bundle args) {
        return new SmsLoader(context, args);
    }

    @Override
    public void onLoadFinished(Loader loader, SmsResult data) {
        Log.d("TEST-ER", "Result: " + data.groupData.size());
        if (resultListener != null) {
            resultListener.onResult(data);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }


}