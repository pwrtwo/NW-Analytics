package com.keyboardape.newwestminsteranalyticsapp.utilities;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * JSON Parser async task.
 */
public class JSONParserAsync extends AsyncTask<Void, Void, Void> {

    private Callbacks         mCallbacks;
    private String            mJsonUrl;
    private boolean           mIsStreamParsed;
    private long              mURLLastModified;

    // globalized to ease cleaning up resources when parsing completes
    private HttpURLConnection mConnection;
    private InputStream       mInputStream;
    private InputStreamReader mInputStreamReader;

    public JSONParserAsync(Callbacks callbacks, String jsonUrl) {
        mCallbacks         = callbacks;
        mJsonUrl           = jsonUrl;
        mIsStreamParsed    = false;
        mURLLastModified   = 0;
        mConnection        = null;
        mInputStream       = null;
        mInputStreamReader = null;
    }

    // ---------------------------------------------------------------------------------------------
    //                                         ASYNC TASK
    // ---------------------------------------------------------------------------------------------/

    @Override
    protected Void doInBackground(Void... voids) {
        if (loadInputStreamReader()) {
            JSONObject jsonObject;
            while ((jsonObject = getNextJsonObjectOrNull()) != null) {
                mCallbacks.onNewJsonObjectFromStream(jsonObject);
            }
            mIsStreamParsed = true;
        }
        cleanUpInputStreamReader();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mCallbacks.onJsonStreamParsed(mIsStreamParsed, mURLLastModified);
    }

    // ---------------------------------------------------------------------------------------------
    //                               GET NEXT JSON OBJECT FROM STREAM
    // ---------------------------------------------------------------------------------------------/

    private JSONObject getNextJsonObjectOrNull() {
        final int OPEN_BRACKET = '{';
        final int CLOSE_BRACKET = '}';
        final int END_OF_STREAM = -1;

        StringBuilder str = new StringBuilder();

        try {
            int openBracketCount = 0;
            int c;

            // skip till END_OF_STREAM or OPEN_BRACKET found
            while ((c = mInputStreamReader.read()) != END_OF_STREAM && c != OPEN_BRACKET) {}

            if (c == END_OF_STREAM) {
                return null;
            }

            // expected OPEN_BRACKET found
            // increment openBracketCount and append to string
            str.append((char) OPEN_BRACKET);
            ++openBracketCount;

            // append all chars to string till openBracketCount == 0
            while (openBracketCount != 0) {
                c = mInputStreamReader.read();
                str.append((char) c);
                if (c == OPEN_BRACKET) {
                    ++openBracketCount;
                } else if (c == CLOSE_BRACKET) {
                    --openBracketCount;
                }
            }
        } catch (Exception e) {
            Log.e(JSONParserAsync.class.getSimpleName(), e.getMessage());
        }

        // return JSONObject or null
        if (str.length() > 0) {
            try {
                return new JSONObject(str.toString());
            } catch (Exception e) {
                // Expected; return null
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------
    //                              LOADING & CLEANING INPUT STREAM READER
    // ---------------------------------------------------------------------------------------------/

    private boolean loadInputStreamReader() {
        try {
            String requestMethod = "GET";
            URL url = new URL(mJsonUrl);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod(requestMethod);
            mURLLastModified = mConnection.getLastModified();
            mInputStream = mConnection.getInputStream();
            mInputStreamReader = new InputStreamReader(mInputStream);
            return true;
        } catch (Exception e) {
            Log.e(JSONParserAsync.class.getSimpleName(), e.getMessage());
        }
        return false;
    }

    private void cleanUpInputStreamReader() {
        try {
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
            Log.e(JSONParserAsync.class.getSimpleName(), e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------
    //          CALLBACK : ON NEW JSON OBJECT FROM STREAM / ON JSON STREAM PARSED CALLBACK
    // ---------------------------------------------------------------------------------------------/

    public interface Callbacks {
        void onNewJsonObjectFromStream(JSONObject o);
        void onJsonStreamParsed(boolean isSuccessful, long dataLastUpdated);
    }
}