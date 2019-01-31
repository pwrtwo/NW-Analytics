package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;
import com.keyboardape.newwestminsteranalyticsapp.utilities.JSONParserAsync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Skytrain Stations DataSet.
 *
 * Downloaded from New West Open Data:
 * http://opendata.newwestcity.ca/datasets/skytrain-stations
 */
public class SkytrainStationsData extends DataSet {

    private final static String              TABLE_NAME;
    private final static String              DATA_SOURCE_URL;
    private final static DataSetType         DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;
    private final static int                 R_STRING_ID_NAME;

    static {
        TABLE_NAME       = "skytrain_stations";
        DATA_SOURCE_URL  = "http://opendata.newwestcity.ca/downloads/skytrain-stations/SKYTRAIN_STATIONS.json";
        DATA_SET_TYPE    = DataSetType.SKYTRAIN_STATIONS;
        R_STRING_ID_NAME = R.string.dataset_skytrain_stations;

        TABLE_COLUMNS = new HashMap<>();
        TABLE_COLUMNS.put("ID",        "INTEGER PRIMARY KEY AUTOINCREMENT");
        // Original Data
        TABLE_COLUMNS.put("NAME",      "TEXT"); // e.g. "NEW WESTMINSTER STATION"
        TABLE_COLUMNS.put("PHASE",     "TEXT"); // e.g. "EXPO"
        TABLE_COLUMNS.put("LATITUDE",  "REAL"); // e.g.   49.20118360787066
        TABLE_COLUMNS.put("LONGITUDE", "REAL"); // e.g. -122.91293786875315
        // Discarded Data:
        //      - ID       (values are all 0)
    }

    public SkytrainStationsData() {
        super(DATA_SOURCE_URL, DATA_SET_TYPE ,TABLE_NAME ,TABLE_COLUMNS, R_STRING_ID_NAME);
    }

    @Override
    protected void downloadDataToDBAsync(final OnDataSetUpdatedCallbackInternal callback) {
        final SQLiteDatabase db = DBHelper.GetInstance().getWritableDatabase();
        new JSONParserAsync(new JSONParserAsync.Callbacks() {
            @Override
            public void onNewJsonObjectFromStream(JSONObject o) {
                try {
                    ContentValues c = new ContentValues();

                    // Original Data
                    c.put("NAME",  ParseToStringOrNull(o.getString("NAME")));
                    c.put("PHASE", ParseToStringOrNull(o.getString("PHASE")));

                    JSONArray coordinates = GetAverageCoordinatesFromJsonGeometryOrNull(o);
                    c.put("LATITUDE", (coordinates == null) ? null : coordinates.getDouble(1));
                    c.put("LONGITUDE",(coordinates == null) ? null : coordinates.getDouble(0));

                    db.insert(TABLE_NAME, null, c);
                } catch (Exception e) {
                    Log.e(SkytrainStationsData.class.getSimpleName(), e.getMessage() + "::" + o.toString());
                }
            }
            @Override
            public void onJsonStreamParsed(boolean isSuccessful, long dataLastUpdated) {
                db.close();
                callback.onDataSetUpdated(DATA_SET_TYPE, isSuccessful, dataLastUpdated);
            }
        }, DATA_SOURCE_URL).execute();
    }
}
