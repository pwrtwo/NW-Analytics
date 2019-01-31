package com.keyboardape.newwestminsteranalyticsapp.utilities;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * CSV Parser async task.
 */
public class CSVParserAsync extends AsyncTask<Void, Void, Void> {

    private Callbacks         mCallbacks;
    private String            mCSVURL;
    private boolean           mIsStreamParsed;
    private long              mURLLastModified;

    // globalized to ease cleaning up resources when parsing completes
    private HttpURLConnection mConnection;
    private InputStream       mInputStream;
    private InputStreamReader mInputStreamReader;
    private BufferedReader    mBufferedReader;

    public CSVParserAsync(Callbacks callbacks, String csvURL) {
        mCallbacks         = callbacks;
        mCSVURL            = csvURL;
        mIsStreamParsed    = false;
        mURLLastModified   = 0;
        mConnection        = null;
        mInputStream       = null;
        mInputStreamReader = null;
        mBufferedReader    = null;
    }

    // ---------------------------------------------------------------------------------------------
    //                                         ASYNC TASK
    // ---------------------------------------------------------------------------------------------/

    @Override
    protected Void doInBackground(Void... voids) {
        if (loadBufferedReader()) {
            String[] csvRow;
            while ((csvRow = getNextCSVRow()) != null) {
                mCallbacks.onNewCSVRowFromStream(csvRow);
            }
            mIsStreamParsed = true;
        }
        cleanUpBufferedReader();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mCallbacks.onCSVStreamParsed(mIsStreamParsed, mURLLastModified);
    }

    // ---------------------------------------------------------------------------------------------
    //                               GET NEXT CSV ROW FROM STREAM
    // ---------------------------------------------------------------------------------------------/

    private String[] getNextCSVRow() {
        try {
            String csvLine = mBufferedReader.readLine();
            if (csvLine != null) {
                return csvLine.split(",");
            }
        } catch (Exception e) {
            Log.e(CSVParserAsync.class.getSimpleName(), e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------
    //                              LOADING & CLEANING INPUT STREAM READER
    // ---------------------------------------------------------------------------------------------/

    private boolean loadBufferedReader() {
        try {
            String requestMethod = "GET";
            URL url = new URL(mCSVURL);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod(requestMethod);
            mURLLastModified = mConnection.getLastModified();
            mInputStream = mConnection.getInputStream();
            mInputStreamReader = new InputStreamReader(mInputStream);
            mBufferedReader = new BufferedReader((mInputStreamReader));
            return true;
        } catch (Exception e) {
            Log.e(CSVParserAsync.class.getSimpleName(), e.getMessage());
        }
        return false;
    }

    private void cleanUpBufferedReader() {
        try {
            if (mBufferedReader != null) {
                mBufferedReader.close();
            }
            if (mInputStreamReader != null) {
                mInputStreamReader.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mConnection != null) {
                mConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(CSVParserAsync.class.getSimpleName(), e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------
    //          CALLBACK : ON NEW JSON OBJECT FROM STREAM / ON JSON STREAM PARSED CALLBACK
    // ---------------------------------------------------------------------------------------------/

    public interface Callbacks {
        void onNewCSVRowFromStream(String[] row);
        void onCSVStreamParsed(boolean isSuccessful, long dataLastUpdated);
    }
}