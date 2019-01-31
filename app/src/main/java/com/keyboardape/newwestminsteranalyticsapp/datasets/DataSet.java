package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.utilities.DBHelper;
import com.keyboardape.newwestminsteranalyticsapp.utilities.GetURLLastUpdateAsync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DataSet.
 *
 * All data set are downloaded from New West Open DataSet:
 * http://opendata.newwestcity.ca/datasets
 */
public abstract class DataSet {

    // ---------------------------------------------------------------------------------------------
    //                                  STATIC : INITIALIZATION
    // ---------------------------------------------------------------------------------------------

    private static boolean                                    IsInitialized;
    private static Map<DataSetType, DataSet>                  DataSetInstances;
    private static Map<DataSetType, Class<? extends DataSet>> DataSetClasses;

    static {
        IsInitialized = false;
        DataSetInstances = new LinkedHashMap<>();
        DataSetClasses = new LinkedHashMap<>();
        DataSetClasses.put(DataSetType.BUS_STOPS,           BusStopsData.class);
        DataSetClasses.put(DataSetType.SKYTRAIN_STATIONS,   SkytrainStationsData.class);
        DataSetClasses.put(DataSetType.BUILDING_ATTRIBUTES, BuildingAttributesData.class);
        DataSetClasses.put(DataSetType.BUSINESS_LICENSES,   BusinessLicensesData.class);
        DataSetClasses.put(DataSetType.MAJOR_SHOPPING,      MajorShoppingData.class);
        DataSetClasses.put(DataSetType.BUILDING_AGE,        BuildingAgeData.class);
//        DataSetClasses.put(DataSetType.HIGH_RISES,          HighRisesData.class);
        DataSetClasses.put(DataSetType.AGE_DEMOGRAPHICS,    AgeDemographicsData.class);
        DataSetClasses.put(DataSetType.SCHOOL_BUILDINGS,    SchoolBuildingsData.class);
    }

    /**
     * Initializes all DataSets.
     * @param context of caller
     */
    public static synchronized void Initialize(Context context) {
        if (!IsInitialized) {
            IsInitialized = true;

            // Special data set requires context for Geocoding
            DataSetInstances.put(DataSetType.BUSINESS_LICENSES, new BusinessLicensesData(context));

            try {
                for (DataSetType type : GetAllDataSetTypes()) {
                    if (DataSetInstances.get(type) == null) {
                        DataSetInstances.put(type, DataSetClasses.get(type).newInstance());
                    }
                }
            } catch (Exception e) {
                Log.e(DataSet.class.getSimpleName(), e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    //                                     STATIC : GETTERS
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns DataSet given a DataSetType.
     * @param dataSetType instance to get
     * @return DataSet
     */
    public static DataSet GetDataSet(DataSetType dataSetType) {
        return DataSetInstances.get(dataSetType);
    }

    /**
     * Returns an array of all valid DataSetTypes.
     * @return DataSetType[]
     */
    public static DataSetType[] GetAllDataSetTypes() {
        return DataSetClasses.keySet().toArray(new DataSetType[DataSetClasses.size()]);
    }

    /**
     * Returns an array of all DataSet instances.
     * @return DataSet[]
     */
    public static DataSet[] GetAllDataSets() {
        return DataSetInstances.values().toArray(new DataSet[DataSetInstances.size()]);
    }

    // ---------------------------------------------------------------------------------------------
    //                                  STATIC : DATA PARSING HELPERS
    // ---------------------------------------------------------------------------------------------

    /**
     * Parses a string to a string or null if string = "null".
     * @param string to be parsed
     * @return string or null
     */
    protected static String ParseToStringOrNull(String string) {
        return (string.length() == 0 || string.equalsIgnoreCase("null"))
            ? null
            : string;
    }

    /**
     * Parses a string to an Integer or null if failed.
     * @param integer to be parsed
     * @return Integer or null
     */
    protected static Integer ParseToIntOrNull(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }

    /**
     * Parses a string to a Double or null if failed.
     * @param aDouble to be parsed
     * @return Double or null
     */
    protected static Double ParseToDoubleOrNull(String aDouble) {
        try {
            return Double.parseDouble(aDouble);
        } catch (Exception e) {
            // Expected; return null
        }
        return null;
    }

    /**
     * Parses and returns an average of all coordinates in a GeoJSON object.
     * @param o JSON object containing a GeoJSON
     * @return JSONArray containing latitude and longitude, or null if failed
     */
    protected static JSONArray GetAverageCoordinatesFromJsonGeometryOrNull(JSONObject o) {
        try {
            JSONObject geoJson = o.getJSONObject("json_geometry");
            String geoJsonType = geoJson.getString("type");

            // different shapes have different # of array layers
            JSONArray coordinates = geoJson.getJSONArray("coordinates"); // a Line
            if (geoJsonType.equals("Polygon")) {
                coordinates = coordinates.getJSONArray(0);
            } else if (geoJsonType.equals("MultiPolygon")) {
                coordinates = coordinates.getJSONArray(0).getJSONArray(0);
            }

            // Get the average of all longitude/latitude coordinates
            int numCoord2 = 0;
            int numCoord1 = 0;
            double coord1 = 0;
            double coord2 = 0;
            try {
                int i = 0;
                if (geoJsonType.equals("Point")) {
                    coord1 += coordinates.getDouble(0);
                    ++numCoord1;
                    coord2 += coordinates.getDouble(1);
                    ++numCoord2;
                } else {
                    while (true) {
                        JSONArray coordinate = coordinates.getJSONArray(i++);
                        coord1 += coordinate.getDouble(0);
                        ++numCoord1;
                        coord2 += coordinate.getDouble(1);
                        ++numCoord2;
                    }
                }
            } catch (Exception e) {
                // Expected to end with exception 100% of time when there's no more coordinates
            }

            return new JSONArray(new double[] {coord1 / numCoord1, coord2 / numCoord2});
        } catch (Exception e) {
            Log.e(DataSet.class.getSimpleName(), e.getMessage() + "::" + o.toString());
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------
    //                                         INSTANCE
    // ---------------------------------------------------------------------------------------------

    private final String              mDataSourceURL;
    private final DataSetType         mDataSetType;
    private final String              mTableName;
    private final Map<String, String> mTableColumns;
    private final int                 mRStringIDName;

    private boolean mIsUpdating;
    private boolean mIsUpToDate;

    /**
     * Constructor.
     * @param dataSetType of this DataSet
     * @param tableName of this DataSet
     * @param tableColumns of this DataSet
     * @param rStringIDName Android string resource ID of this DataSet name
     */
    public DataSet(String dataSourceURL,
                   DataSetType dataSetType,
                   String tableName,
                   Map<String, String> tableColumns,
                   int rStringIDName) {
        mDataSourceURL = dataSourceURL;
        mDataSetType  = dataSetType;
        mTableName    = tableName;
        mTableColumns = tableColumns;
        mRStringIDName = rStringIDName;

        mIsUpdating = false;
        mIsUpToDate = false;
    }

    /**
     * Returns DataSetType of this DataSet.
     * @return DataSetType
     */
    public DataSetType getDataSetType() {
        return mDataSetType;
    }

    /**
     * Returns database table name of this DataSet.
     * @return string
     */
    public String getTableName() {
        return mTableName;
    }

    /**
     * Returns Android string resource ID of this DataSet name.
     * @return Android string resource ID as an int
     */
    public int getRStringIDDataSetName() {
        return mRStringIDName;
    }

    /**
     * Returns true if this DataSet requires update.
     */
    public void isRequireUpdateAsync(OnDataSetRequireUpdate callback) {
        mIsUpdating = true;

        // Check database flag first
        ContentValues c = DataSetTracker.GetStatsOrNull(mDataSetType);
        mIsUpToDate = !((c == null) || c.getAsBoolean("isRequireUpdate"));
        if (!mIsUpToDate) {
            mIsUpdating = false;
            callback.onDataSetRequireUpdate(true);
            return;
        }

        // Check URL for last update time
        new GetURLLastUpdateAsync(new GetURLLastUpdateAsync.Callbacks() {
            @Override
            public void onURLLastUpdateRead(Long dataLastUpdatedOrNull) {
                mIsUpdating = false;
                boolean isRequireUpdate = (dataLastUpdatedOrNull == null) ||
                        (dataLastUpdatedOrNull > c.getAsLong("lastUpdated"));
                callback.onDataSetRequireUpdate(isRequireUpdate);
            }
        }, mDataSourceURL).execute();
    }

    /**
     * Returns true if this DataSet is currently updating.
     * @return true if this DataSet is currently updating
     */
    public boolean isUpdating() {
        return mIsUpdating;
    }

    /**
     * Returns true if this DataSet is up to date. This flag is used to bypass an error thrown
     * from too many DB requests when calling isRequireUpdate() multiple times.
     * @return true if DataSet is up to date
     */
    public boolean isUpToDate() {
        return mIsUpToDate;
    }

    /**
     * Flag/unflag this DataSet for update on next application startup.
     * @param isRequireUpdate true if flagging DataSet for update
     */
    public void setRequireUpdate(boolean isRequireUpdate) {
        DataSetTracker.SetRequireUpdate(mDataSetType, isRequireUpdate, 0);
    }

    // ---------------------------------------------------------------------------------------------
    //                                       UPDATE DATA SET
    // ---------------------------------------------------------------------------------------------

    /**
     * Download data to database in the background.
     * @param callback function when download task completes, success or fail
     */
    abstract protected void downloadDataToDBAsync(OnDataSetUpdatedCallbackInternal callback);

    /**
     * (Re) Downloads data into the database in the background.
     * @param callback function when download task completes, success or fail
     */
    public void updateDataAsync(final OnDataSetUpdatedCallback callback) {
        mIsUpdating = true;
        recreateDBTable();
        downloadDataToDBAsync(new OnDataSetUpdatedCallbackInternal() {
            @Override
            public void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful, long dataLastUpdated) {
                mIsUpdating = false;
                mIsUpToDate = true;
                DataSetTracker.SetRequireUpdate(mDataSetType, !isUpdateSuccessful, dataLastUpdated);
                callback.onDataSetUpdated(dataSetType, isUpdateSuccessful);
            }
        });
    }

    /**
     * Drop and re-create database table.
     */
    private void recreateDBTable() {
        SQLiteDatabase db = DBHelper.GetInstance().getWritableDatabase();
        String csvColumnNamesWithAttributes = concatenateToCSV(mTableColumns);
        String queryDelete = "DROP TABLE IF EXISTS " + mTableName + ";";
        String queryCreate = "CREATE TABLE IF NOT EXISTS " + mTableName + "(" + csvColumnNamesWithAttributes + ");";
        db.execSQL(queryDelete);
        db.execSQL(queryCreate);
        db.close();
    }

    /**
     * Concatenates Map into comma-separated-values as a string.
     * @param map to be concatenated
     * @return string
     */
    private String concatenateToCSV(Map<String, String> map) {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            str.append(entry.getKey())
                    .append(' ')
                    .append(entry.getValue())
                    .append(',');
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    // ---------------------------------------------------------------------------------------------
    //                               INTERFACE : ON DATA SET UPDATED CALLBACK
    // ---------------------------------------------------------------------------------------------/

    /**
     * Callback function when data set requires update.
     */
    public interface OnDataSetRequireUpdate {
        void onDataSetRequireUpdate(boolean isRequireUpdate);
    }

    /**
     * Callback function for caller.
     */
    public interface OnDataSetUpdatedCallback {
        void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful);
    }

    /**
     * Callback function used internally.
     */
    protected interface OnDataSetUpdatedCallbackInternal {
        void onDataSetUpdated(DataSetType dataSetType, boolean isUpdateSuccessful, long dataLastUpdated);
    }
}
