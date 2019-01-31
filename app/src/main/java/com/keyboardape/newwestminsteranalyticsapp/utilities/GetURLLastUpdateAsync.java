package com.keyboardape.newwestminsteranalyticsapp.utilities;

import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Get URL Last Update Time async task.
 */
public class GetURLLastUpdateAsync extends AsyncTask<Void, Void, Void> {

    private Callbacks         mCallbacks;
    private String            mURL;
    private Long              mURLLastModified;

    // globalized to ease cleaning up resources when parsing completes
    private HttpURLConnection mConnection;

    public GetURLLastUpdateAsync(Callbacks callbacks, String url) {
        mCallbacks         = callbacks;
        mURL               = url;
        mURLLastModified   = null;
        mConnection        = null;
    }

    // ---------------------------------------------------------------------------------------------
    //                                         ASYNC TASK
    // ---------------------------------------------------------------------------------------------/

    @Override
    protected Void doInBackground(Void... voids) {
        loadInputStreamReader();
        cleanUpInputStreamReader();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mCallbacks.onURLLastUpdateRead(mURLLastModified);
    }

    // ---------------------------------------------------------------------------------------------
    //                              LOADING & CLEANING INPUT STREAM READER
    // ---------------------------------------------------------------------------------------------/

    private boolean loadInputStreamReader() {
        try {
            String requestMethod = "GET";
            URL url = new URL(mURL);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod(requestMethod);
            mURLLastModified = mConnection.getLastModified();
            return true;
        } catch (Exception e) {
            Log.e(GetURLLastUpdateAsync.class.getSimpleName(), e.getMessage());
        }
        return false;
    }

    private void cleanUpInputStreamReader() {
        try {
            if (mConnection != null) {
                mConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(GetURLLastUpdateAsync.class.getSimpleName(), e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------
    //                           CALLBACK : WHEN LAST UPDATE TIME RETRIEVED
    // ---------------------------------------------------------------------------------------------/

    public interface Callbacks {
        void onURLLastUpdateRead(Long dataLastUpdatedOrNull);
    }
}