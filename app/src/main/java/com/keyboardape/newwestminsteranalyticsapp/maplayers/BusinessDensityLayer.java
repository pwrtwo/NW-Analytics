package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.R;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSetType;
import com.keyboardape.newwestminsteranalyticsapp.utilities.DBReaderAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Business Density MapLayer.
 */
public class BusinessDensityLayer extends MapLayer {

    // ---------------------------------------------------------------------------------------------
    //                                             STATIC
    // ---------------------------------------------------------------------------------------------

    private final static int          R_STRING_ID_LAYER_NAME;
    private final static int          R_DRAWABLE_ID_ICON;
    private final static MapLayerType LAYER_TYPE;
    private final static int          HEATMAP_RADIUS;
    private final static boolean      HAS_SELECTED_AREA_FUNCTIONS;

    static {
        R_STRING_ID_LAYER_NAME      = R.string.layer_business_density;
        R_DRAWABLE_ID_ICON          = R.drawable.ic_business_black_24dp;
        LAYER_TYPE                  = MapLayerType.BUSINESS_DENSITY;
        HEATMAP_RADIUS              = 30;
        HAS_SELECTED_AREA_FUNCTIONS = false;
    }

    // ---------------------------------------------------------------------------------------------
    //                                           INSTANCE
    // ---------------------------------------------------------------------------------------------

    public BusinessDensityLayer() {
        super(LAYER_TYPE, R_STRING_ID_LAYER_NAME, R_DRAWABLE_ID_ICON, HEATMAP_RADIUS, HAS_SELECTED_AREA_FUNCTIONS);
    }

    @Override
    public void getMapDataAsync(final OnMapLayerDataReadyCallback callback) {
        String buildingAgeTableName = DataSetType.BUSINESS_LICENSES.getDataSet().getTableName();
        String sqlQuery = "SELECT LATITUDE, LONGITUDE "
                + "FROM " + buildingAgeTableName + " "
                + "WHERE LATITUDE IS NOT NULL "
                + "AND LONGITUDE IS NOT NULL";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            List<WeightedLatLng> data;
            @Override
            public void onDBCursorReady(Cursor cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        data = new ArrayList<>();
                        do {
                            LatLng latlng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                            WeightedLatLng wlatlng = new WeightedLatLng(latlng, 1);
                            data.add(wlatlng);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(BusinessDensityLayer.class.getSimpleName(), e.getMessage());
                    data = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                callback.onMapLayerDataReady(LAYER_TYPE, data);
            }
        }, sqlQuery).execute();
    }
}
