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
 * Public Transit MapLayer.
 */
public class PublicTransitLayer extends MapLayer {

    // ---------------------------------------------------------------------------------------------
    //                                             STATIC
    // ---------------------------------------------------------------------------------------------

    private final static int          R_STRING_ID_LAYER_NAME;
    private final static int          R_DRAWABLE_ID_ICON;
    private final static MapLayerType LAYER_TYPE;
    private final static int          HEATMAP_RADIUS;
    private final static boolean      HAS_SELECTED_AREA_FUNCTIONS;

    static {
        R_STRING_ID_LAYER_NAME      = R.string.layer_public_transit;
        R_DRAWABLE_ID_ICON          = R.drawable.ic_traffic_black_24dp;
        LAYER_TYPE                  = MapLayerType.PUBLIC_TRANSIT;
        HEATMAP_RADIUS              = 25;
        HAS_SELECTED_AREA_FUNCTIONS = false;
    }

    // ---------------------------------------------------------------------------------------------
    //                                           INSTANCE
    // ---------------------------------------------------------------------------------------------

    public PublicTransitLayer () {
        super(LAYER_TYPE, R_STRING_ID_LAYER_NAME, R_DRAWABLE_ID_ICON, HEATMAP_RADIUS, HAS_SELECTED_AREA_FUNCTIONS);
    }

    @Override
    public void getMapDataAsync(final OnMapLayerDataReadyCallback callback) {
        String skytrainStationsTableName = DataSetType.SKYTRAIN_STATIONS.getDataSet().getTableName();
        String sqlQuery =
                "SELECT LATITUDE, LONGITUDE " +
                "FROM '" + skytrainStationsTableName + "' " +
                "WHERE LATITUDE IS NOT NULL " +
                "  AND LONGITUDE IS NOT NULL";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            List<WeightedLatLng> data;
            @Override
            public void onDBCursorReady(Cursor cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        data = new ArrayList<>();
                        do {
                            LatLng latlng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                            WeightedLatLng wlatlng = new WeightedLatLng(latlng, 5);
                            data.add(wlatlng);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(PublicTransitLayer.class.getSimpleName(), e.getMessage());
                    data = null;
                }
            }
            @Override
            public void onDBReadComplete() {
                if (data == null) {
                    callback.onMapLayerDataReady(LAYER_TYPE, data);
                } else {
                    getMapData2Async(callback, data);
                }
            }
        }, sqlQuery).execute();
    }

    public void getMapData2Async(final OnMapLayerDataReadyCallback callback, final List<WeightedLatLng> data) {
        String busStopsTableName = DataSetType.BUS_STOPS.getDataSet().getTableName();
        String sqlQuery =
                "SELECT LATITUDE, LONGITUDE " +
                "FROM '" + busStopsTableName + "' " +
                "WHERE LATITUDE IS NOT NULL " +
                "  AND LONGITUDE IS NOT NULL";
        new DBReaderAsync(new DBReaderAsync.Callbacks() {
            boolean isSuccessful = true;
            @Override
            public void onDBCursorReady(Cursor cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            LatLng latlng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                            WeightedLatLng wlatlng = new WeightedLatLng(latlng, 1);
                            data.add(wlatlng);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(PublicTransitLayer.class.getSimpleName(), e.getMessage());
                    isSuccessful = false;
                }
            }
            @Override
            public void onDBReadComplete() {
                callback.onMapLayerDataReady(LAYER_TYPE, (isSuccessful) ? data : null);
            }
        }, sqlQuery).execute();
    }
}
