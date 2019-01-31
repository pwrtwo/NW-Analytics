package com.keyboardape.newwestminsteranalyticsapp.maplayers;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.keyboardape.newwestminsteranalyticsapp.maplayerinfo.MapLayerInfoFragment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MapLayer.
 */
public abstract class MapLayer {

    // ---------------------------------------------------------------------------------------------
    //                                   STATIC : INITIALIZATION
    // ---------------------------------------------------------------------------------------------

    /** GoogleMap instance. */
    protected static GoogleMap                                  GMap;

    /** Polygon of selected area. */
    private static Polygon                                      SelectedAreaPolygon;
    /** Coordinates of selected area. */
    private static LatLng[]                                     SelectedAreaCoordinates;
    /** Center point to move to. */
    private static LatLng                                       CenterPoint;
    /** Zoom level of saved CenterPoint */
    private static float                                        CenterPointZoomLevel;

    /** Tracks if MapLayer is initialized or not. */
    private static boolean                                      IsInitialized;
    /** All MapLayer instances. */
    private static Map<MapLayerType, MapLayer>                  LayerInstances;
    /** All MapLayer classes. */
    private static Map<MapLayerType, Class<? extends MapLayer>> LayerClasses;

    static {
        GMap = null;
        SelectedAreaPolygon = null;
        SelectedAreaCoordinates = null;
        CenterPoint = null;

        LayerInstances = new LinkedHashMap<>();

        LayerClasses = new LinkedHashMap<>();
        LayerClasses.put(MapLayerType.POPULATION_DENSITY, PopulationDensityLayer.class);
        LayerClasses.put(MapLayerType.BUILDING_AGE,       BuildingAgeLayer.class);
//        LayerClasses.put(MapLayerType.HIGH_RISES,         HighRisesLayer.class);
        LayerClasses.put(MapLayerType.BUSINESS_DENSITY,   BusinessDensityLayer.class);
        LayerClasses.put(MapLayerType.PUBLIC_TRANSIT,     PublicTransitLayer.class);
        LayerClasses.put(MapLayerType.SCHOOL_BUILDINGS,   SchoolBuildingsLayer.class);
    }

    /**
     * Initialize an instance for every MapLayer.
     */
    public static synchronized void Initialize() {
        if (!IsInitialized) {
            IsInitialized = true;
            try {
                for (MapLayerType type : GetAllMapLayerTypes()) {
                    LayerInstances.put(type, LayerClasses.get(type).newInstance());
                }
            } catch (Exception e) {
                Log.e(MapLayer.class.getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Reset all MapLayers if MapsActivity has been stopped.
     * Fixes a bug where sometimes MapLayer would not load on Activity resume.
     */
    public static synchronized void SetActivityStopped() {
        ClearSelectedArea();
        for (MapLayer layer : LayerInstances.values()) {
            layer.resetLayer();
        }
    }

    /**
     * Set GoogleMap instance.
     * @param gMap GoogleMap instance
     */
    public static void SetGoogleMap(GoogleMap gMap) {
        GMap = gMap;
    }

    // ---------------------------------------------------------------------------------------------
    //                                    OTHER STATIC FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    /**
     * Return instance of MapLayer for specified MapLayerType.
     * @param mapLayerType to return
     * @return MapLayer
     */
    public static MapLayer GetLayer(MapLayerType mapLayerType) {
        return LayerInstances.get(mapLayerType);
    }

    /**
     * Return all MapLayerTypes.
     * @return MapLayerType[]
     */
    public static MapLayerType[] GetAllMapLayerTypes() {
        return LayerClasses.keySet().toArray(new MapLayerType[LayerClasses.size()]);
    }

    /**
     * Return all MapLayer instances.
     * @return MapLayer[]
     */
    public static MapLayer[] GetAllMapLayers() {
        return LayerInstances.values().toArray(new MapLayer[LayerInstances.size()]);
    }

    /**
     * Animate to selected area center point.
     */
    public static void AnimateToSelectedArea() {
        if (CenterPoint != null) {
            GMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CenterPoint, CenterPointZoomLevel));
        }
    }

    /**
     * Returns selected area or null.
     * @return LatLng[]
     */
    public static LatLng[] GetSelectedAreaOrNull() {
        return SelectedAreaCoordinates;
    }

    /**
     * Clear selected area.
     */
    public static void ClearSelectedArea() {
        if (SelectedAreaPolygon != null) {
            SelectedAreaPolygon.remove();
            SelectedAreaPolygon = null;
        }
        if (SelectedAreaCoordinates != null) {
            SelectedAreaCoordinates = null;
        }
        if (CenterPoint != null) {
            CenterPoint = null;
        }
    }

    // ---------------------------------------------------------------------------------------------
    //                                         INSTANCE
    // ---------------------------------------------------------------------------------------------

    /** MapLayerType. */
    private MapLayerType         mLayerType;

    /** Android resource id: MapLayer name. */
    private int                  mRStringIDLayerName;
    /** Android resource id: MapLayer icon. */
    private int                  mRDrawableIDIcon;

    /** Heatmap spread radius for this MapLayer. */
    private int                  mHeatmapRadius;
    /** MapLayer's TileOverlay instance. */
    private TileOverlay          mTileOverlay;

    /** Tracks whether this MapLayer has functions related to selected areas. */
    private boolean              mHasSelectedAreaFunctions;

    /**
     * Constructor.
     * @param layerType of MapLayer
     * @param rStringIDLayerName Android resource id: MapLayer name
     * @param rDrawableIDIcon Android resource id: MapLayer icon
     * @param heatmapRadius Heatmap spread radius for MapLayer
     */
    public MapLayer(MapLayerType layerType
                   ,int rStringIDLayerName
                   ,int rDrawableIDIcon
                   ,int heatmapRadius
                   ,boolean hasSelectedAreaFunctions) {

        mLayerType                = layerType;
        mRStringIDLayerName       = rStringIDLayerName;
        mRDrawableIDIcon          = rDrawableIDIcon;
        mHeatmapRadius            = heatmapRadius;
        mTileOverlay              = null;
        mHasSelectedAreaFunctions = hasSelectedAreaFunctions;
    }

    /**
     * Returns MapLayerType of this MapLayer.
     * @return MapLayerType
     */
    public MapLayerType getMapLayerType() {
        return mLayerType;
    }

    /**
     * Returns MapLayerInfoFragment or null.
     * @return MapLayerInfoFragment or null
     */
    public MapLayerInfoFragment getMapLayerInfoFragmentOrNull() {
        return MapLayerInfoFragment.GetFragmentOrNull(mLayerType);
    }

    /**
     * Returns true if MapLayer has selected area functions.
     * @return true if has selected area functions
     */
    public boolean hasSelectedAreaFunctions() {
        return mHasSelectedAreaFunctions;
    }

    /**
     * Returns Android resource id of MapLayer name.
     * @return int
     */
    public int getRStringIDLayerName() {
        return mRStringIDLayerName;
    }

    /**
     * Returns Android resource id of MapLayer icon.
     * @return int
     */
    public int getRDrawableIDIcon() {
        return mRDrawableIDIcon;
    }

    /**
     * Shows the TileOverlay onto the GoogleMap instance.
     */
    public void showLayer() {

        // TileOverlay
        if (mTileOverlay == null) {
            getMapDataAsync(new OnMapLayerDataReadyCallback() {
                @Override
                public void onMapLayerDataReady(MapLayerType mapLayerType, List<WeightedLatLng> dataOrNull) {
                    HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                            .weightedData(dataOrNull)
                            .radius(mHeatmapRadius)
                            .build();
                    mTileOverlay = GMap.addTileOverlay(
                            new TileOverlayOptions()
                                    .fadeIn(false)
                                    .tileProvider(provider)
                            );
                }
            });
        } else {
            mTileOverlay.setVisible(true);
        }

        // Selected Area
        if (mHasSelectedAreaFunctions && SelectedAreaPolygon != null) {
            SelectedAreaPolygon.setVisible(true);
        }
    }

    /**
     * Hides the TileOverlay from the GoogleMap instance.
     */
    public void hideLayer() {

        // TileOverlay
        if (mTileOverlay != null) {
            mTileOverlay.setVisible(false);
        }

        // Selected Area
        if (mHasSelectedAreaFunctions && SelectedAreaPolygon != null) {
            SelectedAreaPolygon.setVisible(false);
        }
    }

    /**
     * Resets MapLayer cache. (Fixes a bug that shows an empty TileOverlay on Activity resume).
     */
    public void resetLayer() {
        if (mTileOverlay != null) {
            mTileOverlay.remove();
            mTileOverlay = null;

            MapLayerInfoFragment layerInfo = MapLayerInfoFragment.GetFragmentOrNull(mLayerType);
            if (layerInfo != null) {
                layerInfo.reloadLayerInfo();
            }
        }
    }

    /**
     * Called when MapLayer has been clicked.
     * @param p coordinate point where user clicked
     * @return true: will show MapLayerInfoFragment, false: will not show MapLayerInfoFragment
     */
    public boolean onMapClick(LatLng p) {
        if (!mHasSelectedAreaFunctions) {
            return false;
        }
        MapLayerInfoFragment layerInfo = MapLayerInfoFragment.GetFragmentOrNull(mLayerType);
        if (layerInfo == null) {
            return false;
        }

        ClearSelectedArea();

        // Calculate multiplier to show same rectangle size on screen, disregarding zoom level
        float zoomLevel = GMap.getCameraPosition().zoom;
        float zoomMultiplier = zoomLevel * 50f * (float) Math.pow(0.48, zoomLevel);
        float xOffset = 0.2f * zoomMultiplier;
        float yOffset = 0.13f * zoomMultiplier;

        // Calculate rectangle coordinates
        SelectedAreaCoordinates = new LatLng[4];
        SelectedAreaCoordinates[0] = new LatLng(p.latitude - yOffset, p.longitude - xOffset);
        SelectedAreaCoordinates[1] = new LatLng(p.latitude - yOffset, p.longitude + xOffset);
        SelectedAreaCoordinates[2] = new LatLng(p.latitude + yOffset, p.longitude + xOffset);
        SelectedAreaCoordinates[3] = new LatLng(p.latitude + yOffset, p.longitude - xOffset);

        // Draw rectangle on map
        SelectedAreaPolygon = GMap.addPolygon(new PolygonOptions()
                .add(SelectedAreaCoordinates[0], SelectedAreaCoordinates[1], SelectedAreaCoordinates[2], SelectedAreaCoordinates[3], SelectedAreaCoordinates[0])
                .strokeColor(Color.RED)
                .strokeWidth(6));

        // Center rectangle on screen
        CenterPoint = new LatLng(p.latitude - (3.5 * yOffset), p.longitude);
        CenterPointZoomLevel = zoomLevel;
        AnimateToSelectedArea();

        // Reload MapLayerInfoFragment
        layerInfo.reloadLayerInfo();

        // Automatically launch Map Layer Info Fragment
        return true;
    }

    /**
     * Gets MapLayer data asynchronously.
     * @param callback function when data is ready
     */
    abstract public void getMapDataAsync(final OnMapLayerDataReadyCallback callback);

    // ---------------------------------------------------------------------------------------------
    //                                  CALLBACK: ON MAP LAYER DATA READY
    // ---------------------------------------------------------------------------------------------

    protected interface OnMapLayerDataReadyCallback {
        void onMapLayerDataReady(MapLayerType mapLayerType, List<WeightedLatLng> dataOrNull);
    }
}
