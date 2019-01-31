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
 * Building Attributes DataSet.
 *
 * Downloaded from New West Open Data:
 * http://opendata.newwestcity.ca/datasets/building-attributes
 */
public class BuildingAttributesData extends DataSet {

    private final static String              TABLE_NAME;
    private final static String              DATA_SOURCE_URL;
    private final static DataSetType         DATA_SET_TYPE;
    private final static Map<String, String> TABLE_COLUMNS;
    private final static int                 R_STRING_ID_NAME;

    static {
        TABLE_NAME       = "building_attributes";
        DATA_SOURCE_URL  = "http://opendata.newwestcity.ca/downloads/building-attributes/BUILDING_ATTRIBUTES.json";
        DATA_SET_TYPE    = DataSetType.BUILDING_ATTRIBUTES;
        R_STRING_ID_NAME = R.string.dataset_building_attributes;

        TABLE_COLUMNS = new HashMap<>();
        TABLE_COLUMNS.put("ID",         "INTEGER PRIMARY KEY AUTOINCREMENT");
        // Original Data
        TABLE_COLUMNS.put("STRNUM",     "TEXT");    // e.g. "211"
        TABLE_COLUMNS.put("STRNAM",     "TEXT");    // e.g. "TWELFTH ST"
        TABLE_COLUMNS.put("BLDG_ID",    "INTEGER"); // e.g. 2958
        TABLE_COLUMNS.put("MAPREF",     "INTEGER"); // e.g. 5574000
        TABLE_COLUMNS.put("NUM_A_GRND", "INTEGER"); // e.g. 5
        TABLE_COLUMNS.put("NUM_MEZZ",   "INTEGER"); // e.g. 0
        TABLE_COLUMNS.put("NUM_B_GRND", "INTEGER"); // e.g. 0
        TABLE_COLUMNS.put("NUM_RES",    "INTEGER"); // e.g. 40
        TABLE_COLUMNS.put("SQM_SITCVR", "REAL");    // e.g. 952.26
        TABLE_COLUMNS.put("SQM_FTPRNT", "REAL");    // e.g. 696.77
        TABLE_COLUMNS.put("SQM_A_GRND", "REAL");    // e.g. 3968.82
        TABLE_COLUMNS.put("SQM_B_GRND", "REAL");    // e.g. 0
        TABLE_COLUMNS.put("LATITUDE",   "REAL");    // e.g.   49.2233306124747
        TABLE_COLUMNS.put("LONGITUDE",  "REAL");    // e.g. -122.912645675014
        // Discarded Data:
        //      - UNITNUM       (values are usually null)
        //      - json_geometry (multiple latitude and longitude points)
    }

    public BuildingAttributesData() {
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
                    Integer numResidents = ParseToIntOrNull(o.getString("NUM_RES"));
                    c.put("STRNAM",     ParseToStringOrNull(o.getString("STRNAM")));
                    c.put("STRNUM",     ParseToStringOrNull(o.getString("STRNUM")));
                    c.put("BLDG_ID",    ParseToIntOrNull(o.getString("BLDG_ID")));
                    c.put("MAPREF",     ParseToIntOrNull(o.getString("MAPREF")));
                    c.put("NUM_RES",    numResidents);
                    c.put("NUM_B_GRND", ParseToIntOrNull(o.getString("NUM_B_GRND")));
                    c.put("NUM_A_GRND", ParseToIntOrNull(o.getString("NUM_A_GRND")));
                    c.put("NUM_MEZZ",   ParseToIntOrNull(o.getString("NUM_MEZZ")));
                    c.put("SQM_SITCVR", ParseToDoubleOrNull(o.getString("SQM_SITCVR")));
                    c.put("SQM_FTPRNT", ParseToDoubleOrNull(o.getString("SQM_FTPRNT")));
                    c.put("SQM_B_GRND", ParseToDoubleOrNull(o.getString("SQM_B_GRND")));
                    c.put("SQM_A_GRND", ParseToDoubleOrNull(o.getString("SQM_A_GRND")));

                    JSONArray coordinates = GetAverageCoordinatesFromJsonGeometryOrNull(o);
                    c.put("LATITUDE", (coordinates == null) ? null : coordinates.getDouble(1));
                    c.put("LONGITUDE",(coordinates == null) ? null : coordinates.getDouble(0));

                    db.insert(TABLE_NAME, null, c);
                } catch (Exception e) {
                    Log.e(BuildingAttributesData.class.getSimpleName(), e.getMessage() + "::" + o.toString());
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
