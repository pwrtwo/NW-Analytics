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
 * Major Shopping DataSet.
 *
 * Downloaded from New West Open Data:
 * http://opendata.newwestcity.ca/datasets/major-shopping
 */
public class MajorShoppingData extends DataSet {

    private final static String              TABLE_NAME;
    private final static String              DATA_SOURCE_URL;
    private final static DataSetType         DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;
    private final static int                 R_STRING_ID_NAME;

    static {
        TABLE_NAME       = "major_shoppings";
        DATA_SOURCE_URL  = "http://opendata.newwestcity.ca/downloads/major-shopping/MAJOR_SHOPPING.json";
        DATA_SET_TYPE    = DataSetType.MAJOR_SHOPPING;
        R_STRING_ID_NAME = R.string.dataset_major_shoppings;

        TABLE_COLUMNS = new HashMap<>();
        TABLE_COLUMNS.put("ID",        "INTEGER PRIMARY KEY AUTOINCREMENT");
        // Original Data
        TABLE_COLUMNS.put("CATEGORY",  "TEXT");    // e.g. "Major Shopping"
        TABLE_COLUMNS.put("STRNUM",    "TEXT");    // e.g. "825"
        TABLE_COLUMNS.put("STRNAM",    "TEXT");    // e.g. "MCBRIDE BLVD"
        TABLE_COLUMNS.put("BLDGNAM",   "TEXT");    // e.g. "Mcbride Plaza Shopping Centre"
        TABLE_COLUMNS.put("BLDG_ID",   "INTEGER"); // e.g. 6510
        TABLE_COLUMNS.put("MAPREF",    "INTEGER"); // e.g. 11226000
        TABLE_COLUMNS.put("LATITUDE",  "REAL");    // e.g.   49.2233306124747
        TABLE_COLUMNS.put("LONGITUDE", "REAL");    // e.g. -122.912645675014
    }

    public MajorShoppingData() {
        super(DATA_SOURCE_URL, DATA_SET_TYPE ,TABLE_NAME ,TABLE_COLUMNS, R_STRING_ID_NAME);
    }

    // ---------------------------------------------------------------------------------------------
    //                                         DOWNLOAD DATA
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void downloadDataToDBAsync(final OnDataSetUpdatedCallbackInternal callback) {
        final SQLiteDatabase db = DBHelper.GetInstance().getWritableDatabase();
        new JSONParserAsync(new JSONParserAsync.Callbacks() {
            @Override
            public void onNewJsonObjectFromStream(JSONObject o) {
                try {
                    ContentValues c = new ContentValues();

                    // Original Data
                    c.put("CATEGORY",  ParseToStringOrNull(o.getString("CATEGORY")));
                    c.put("STRNUM",    ParseToStringOrNull(o.getString("STRNUM")));
                    c.put("STRNAM",    ParseToStringOrNull(o.getString("STRNAM")));
                    c.put("BLDGNAM",   ParseToStringOrNull(o.getString("BLDGNAM")));
                    c.put("BLDG_ID",   ParseToIntOrNull("BLDG_ID"));
                    c.put("MAPREF",    ParseToIntOrNull("MAPREF"));

                    JSONArray coordinates = GetAverageCoordinatesFromJsonGeometryOrNull(o);
                    c.put("LATITUDE", (coordinates == null) ? null : coordinates.getDouble(1));
                    c.put("LONGITUDE",(coordinates == null) ? null : coordinates.getDouble(0));

                    db.insert(TABLE_NAME, null, c);
                } catch (Exception e) {
                    Log.e(MajorShoppingData.class.getSimpleName(), e.getMessage() + "::" + o.toString());
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
