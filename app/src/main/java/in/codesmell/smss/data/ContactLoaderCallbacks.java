package in.codesmell.smss.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

import in.codesmell.smss.utils.ResultListener;

public class ContactLoaderCallbacks implements LoaderManager.LoaderCallbacks<ContactResult> {
    private Context context;
    private ResultListener<ContactResult> resultListener;

    public ContactLoaderCallbacks(Context context, ResultListener<ContactResult> resultListener) {
        this.context = context;
        this.resultListener = resultListener;
    }

    @Override
    public Loader<ContactResult> onCreateLoader(int id, Bundle args) {
        return new ContactLoader(context, args);
    }

    @Override
    public void onLoadFinished(Loader loader, ContactResult data) {
        Log.d("TEST-ER", "ContactsLoader Result: " + data.contacts.size());
        if (resultListener != null) {
            resultListener.onResult(data);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

}