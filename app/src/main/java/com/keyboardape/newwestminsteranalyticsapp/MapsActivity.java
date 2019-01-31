package com.keyboardape.newwestminsteranalyticsapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.keyboardape.newwestminsteranalyticsapp.datasets.DataSet;
import com.keyboardape.newwestminsteranalyticsapp.maplayerinfo.MapLayerInfoFragment;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayer;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayerType;
import com.keyboardape.newwestminsteranalyticsapp.maplayers.MapLayersListFragment;

public class      MapsActivity
       extends    DBActivity
       implements OnMapReadyCallback,
                  GoogleMap.OnMapClickListener {

    private static final LatLngBounds PANNING_BOUNDARY;
    private static final LatLng       NEW_WEST_COORDINATE;
    private static final MapLayerType DEFAULT_MAP_LAYER_TYPE;
    private static final float        INITIAL_ZOOM_LEVEL;
    private static final float        MIN_ZOOM_LEVEL;
    private static final float        MAX_ZOOM_LEVEL;

    static {
        PANNING_BOUNDARY = new LatLngBounds(
                new LatLng(49.162589, -122.957891),
                new LatLng(49.239221, -122.887576)
                );
        NEW_WEST_COORDINATE    = new LatLng(49.205717899999996, -122.910956);
        DEFAULT_MAP_LAYER_TYPE = MapLayerType.POPULATION_DENSITY;
        INITIAL_ZOOM_LEVEL     = 13f;
        MIN_ZOOM_LEVEL         = 13f;
        MAX_ZOOM_LEVEL         = 20f;
    }

    private MapLayerType          mLastMapLayerType = null;
    private MapLayerType          mCurrentMapLayerType = null;

    private GoogleMap             mGMap;
    private MapLayersListFragment mMapLayerFragment;

    private FloatingActionButton  mMapListFAB;
    private FloatingActionButton  mMapInfoFAB;
    private FloatingActionButton  mClearSelectedAreaFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize resources
        DataSet.Initialize(this);
        MapLayer.Initialize();
        MapLayerInfoFragment.Initialize();
        initializeEventListeners();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        MapLayer.SetGoogleMap(googleMap);

        mGMap = googleMap;
        mGMap.setOnMapClickListener(this);
        mGMap.getUiSettings().setRotateGesturesEnabled(false);
        mGMap.setLatLngBoundsForCameraTarget(PANNING_BOUNDARY);
        mGMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NEW_WEST_COORDINATE, INITIAL_ZOOM_LEVEL));
        mGMap.setMinZoomPreference(MIN_ZOOM_LEVEL);
        mGMap.setMaxZoomPreference(MAX_ZOOM_LEVEL);
        loadLayer(DEFAULT_MAP_LAYER_TYPE);
    }

    @Override
    public void onMapClick(LatLng point) {
        if (mCurrentMapLayerType != null && mCurrentMapLayerType.getLayer().onMapClick(point)) {
            autoEnableDisableClearSelectedAreaFAB();
            showMapInfoFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGMap != null) {
            loadLayer(mCurrentMapLayerType);
            autoEnableDisableClearSelectedAreaFAB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MapLayer.SetActivityStopped();
    }

    private void autoEnableDisableClearSelectedAreaFAB() {
        if (MapLayer.GetSelectedAreaOrNull() != null) {
            mClearSelectedAreaFAB.setEnabled(true);
            mClearSelectedAreaFAB.setBackgroundTintList(ColorStateList.valueOf(-16738680));
        } else {
            mClearSelectedAreaFAB.setEnabled(false);
            mClearSelectedAreaFAB.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }
    }

    public void showMapInfoFragment() {
        MapLayerInfoFragment infoFragment = MapLayerInfoFragment.GetFragmentOrNull(mCurrentMapLayerType);
        if (infoFragment != null && !infoFragment.isAdded()) {
            mMapListFAB.setVisibility(View.GONE);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                    .add(R.id.overlay_fragment_container, infoFragment)
                    .commit();
            mMapInfoFAB.setImageResource(R.drawable.ic_close_black_24dp);

            if (mCurrentMapLayerType.getLayer().hasSelectedAreaFunctions()) {
                MapLayer.AnimateToSelectedArea();
            }
        }
    }

    public void hideMapInfoFragment() {
        MapLayerInfoFragment infoFragment = MapLayerInfoFragment.GetFragmentOrNull(mCurrentMapLayerType);
        if (infoFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                    .remove(infoFragment)
                    .commit();
            mMapInfoFAB.setImageResource(R.drawable.ic_info_outline_black_24dp);
            mMapListFAB.setVisibility(View.VISIBLE);

            if (mCurrentMapLayerType.getLayer().hasSelectedAreaFunctions()) {
                mClearSelectedAreaFAB.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showMapLayersList() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                .add(R.id.overlay_fragment_container, mMapLayerFragment)
                .commit();
        mMapListFAB.setImageResource(R.drawable.ic_close_black_24dp);
    }

    public void hideMapLayersList() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                .remove(mMapLayerFragment)
                .commit();
        mMapListFAB.setImageResource(mCurrentMapLayerType.getLayer().getRDrawableIDIcon());
        if (mCurrentMapLayerType.getLayer().hasSelectedAreaFunctions()) {
            mClearSelectedAreaFAB.setVisibility(View.VISIBLE);
        } else {
            mClearSelectedAreaFAB.setVisibility(View.GONE);
        }
    }

    public void loadLayer(MapLayerType mapLayerType) {
        if (mapLayerType == null) {
            return;
        }

        // Update current/last layer types
        if (mCurrentMapLayerType != mapLayerType) {
            mLastMapLayerType = mCurrentMapLayerType;
            mCurrentMapLayerType = mapLayerType;
            mMapLayerFragment.setActiveLayer(mCurrentMapLayerType);
        }

        // Hide last layer, show current layer
        if (mLastMapLayerType != null) {
            mLastMapLayerType.getLayer().hideLayer();
        }
        mCurrentMapLayerType.getLayer().showLayer();

        // Update activity subtitle and Map List FAB button
        getSupportActionBar().setTitle(getString(mCurrentMapLayerType.getLayer().getRStringIDLayerName()));
        FloatingActionButton openFabLayersBtn = (FloatingActionButton) findViewById(R.id.fab_layers);
        openFabLayersBtn.setImageResource(mCurrentMapLayerType.getLayer().getRDrawableIDIcon());

        // Show/hide Map Info Fragment FAB button
        if (mCurrentMapLayerType.getLayer().getMapLayerInfoFragmentOrNull() == null) {
            mMapInfoFAB.setVisibility(View.GONE);
            if (mCurrentMapLayerType.getLayer().hasSelectedAreaFunctions()) {
                mClearSelectedAreaFAB.setVisibility(View.GONE);
            }
        } else {
            mMapInfoFAB.setVisibility(View.VISIBLE);
            if (mCurrentMapLayerType.getLayer().hasSelectedAreaFunctions()) {
                mClearSelectedAreaFAB.setVisibility(View.VISIBLE);
            } else {
                mClearSelectedAreaFAB.setVisibility(View.GONE);
            }
        }
    }

    private void initializeEventListeners() {
        // Map Layer Info Button
        mMapInfoFAB = (FloatingActionButton) findViewById(R.id.fab_layers_info);
        mMapInfoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapLayerInfoFragment fragment = mCurrentMapLayerType.getLayer().getMapLayerInfoFragmentOrNull();
                if (fragment == null) {
                    return;
                }

                if (fragment.isAdded()) {
                    hideMapInfoFragment();
                } else {
                    if (mMapLayerFragment.isVisible()) {
                        hideMapLayersList();
                    }
                    showMapInfoFragment();
                }
            }
        });

        // Clear Selected Area Button
        mClearSelectedAreaFAB = (FloatingActionButton) findViewById(R.id.fab_clear_selected_area);
        autoEnableDisableClearSelectedAreaFAB();
        mClearSelectedAreaFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentMapLayerType.getLayer().hasSelectedAreaFunctions()) {
                    MapLayer.ClearSelectedArea();
                    mCurrentMapLayerType.getLayer().getMapLayerInfoFragmentOrNull().reloadLayerInfo();
                    autoEnableDisableClearSelectedAreaFAB();
                }
            }
        });

        // Map Layer Buttons Fragment
        mMapLayerFragment = new MapLayersListFragment();
        mMapLayerFragment.setActiveLayer(DEFAULT_MAP_LAYER_TYPE);
        mMapListFAB = (FloatingActionButton) findViewById(R.id.fab_layers);
        mMapListFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMapLayerFragment.isVisible()) {
                    if (mCurrentMapLayerType.getLayer().getMapLayerInfoFragmentOrNull() != null) {
                        mMapInfoFAB.setVisibility(View.VISIBLE);
                        mClearSelectedAreaFAB.setVisibility(View.VISIBLE);
                    }
                    hideMapLayersList();
                } else {
                    if (mCurrentMapLayerType.getLayer().getMapLayerInfoFragmentOrNull() != null) {
                        mMapInfoFAB.setVisibility(View.GONE);
                        mClearSelectedAreaFAB.setVisibility(View.GONE);
                    }
                    showMapLayersList();
                }
            }
        });
        mMapListFAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!mMapLayerFragment.isVisible()) {
                    loadLayer(mLastMapLayerType);
                    return true;
                }
                return false;
            }
        });
    }
}
