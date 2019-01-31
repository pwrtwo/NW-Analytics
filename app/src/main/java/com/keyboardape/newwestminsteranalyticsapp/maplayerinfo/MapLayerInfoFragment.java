package com.keyboardape.newwestminsteranalyticsapp.maplayerinfo;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayer;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MapLayerInfoFragment.
 *
 * Displayed in MapsActivity for some MapLayers.
 */
public abstract class MapLayerInfoFragment extends Fragment {

    // ---------------------------------------------------------------------------------------------
    //                                           STATIC
    // ---------------------------------------------------------------------------------------------

    /** Tracks whether MapLayerInfoFragment is initialized or not. */
    private static boolean                                                  IsInitialized;

    /** All MapLayerInfoFragment instances. */
    private static Map<MapLayerType, MapLayerInfoFragment>                  LayerInfoFragmentInstances;

    /** All MapLayerInfoFragment classes. */
    private static Map<MapLayerType, Class<? extends MapLayerInfoFragment>> LayerInfoFragmentClasses;

    static {
        IsInitialized = false;
        LayerInfoFragmentInstances = new LinkedHashMap<>();

        LayerInfoFragmentClasses = new LinkedHashMap<>();
        LayerInfoFragmentClasses.put(MapLayerType.POPULATION_DENSITY, PopulationDensityFragment.class);
        LayerInfoFragmentClasses.put(MapLayerType.BUILDING_AGE,       BuildingAgeFragment.class);
    }

    /**
     * Initialize an instance for every MapLayerInfoFragment.
     */
    public static void Initialize() {
        if (!IsInitialized) {
            IsInitialized = true;
            try {
                for (MapLayerType type : LayerInfoFragmentClasses.keySet()) {
                    LayerInfoFragmentInstances.put(type, LayerInfoFragmentClasses.get(type).newInstance());
                }
            } catch (Exception e) {
                Log.e(MapLayerInfoFragment.class.getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Returns instance of MapLayerInfoFragment or null.
     * @param mapLayerType of MapLayerInfoFragment
     * @return MapLayerInfoFragment or null
     */
    public static MapLayerInfoFragment GetFragmentOrNull(MapLayerType mapLayerType) {
        return LayerInfoFragmentInstances.get(mapLayerType);
    }

    // ---------------------------------------------------------------------------------------------
    //                                         INSTANCE
    // ---------------------------------------------------------------------------------------------

    /**
     * Empty constructor.
     */
    public MapLayerInfoFragment() {
    }

    /**
     * Reloads MapLayerInfoFragment.
     */
    public abstract void reloadLayerInfo();
}
