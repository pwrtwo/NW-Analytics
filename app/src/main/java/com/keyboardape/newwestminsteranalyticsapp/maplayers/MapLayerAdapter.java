package com.keyboardape.newwestminsteranalyticsapp.maplayers;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.keyboardape.newwestminsteranalyticsapp.R;

/**
 * MapLayerAdapter.
 *
 * Used for display a list of MapLayers.
 */
public class MapLayerAdapter extends ArrayAdapter<MapLayer> {

    private MapLayerType mActiveMapLayerType;

    public MapLayerAdapter(Context context, MapLayer[] layers, MapLayerType activeMapLayerType) {
        super(context, 0, layers);
        mActiveMapLayerType = activeMapLayerType;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            MapLayer layer = getItem(position);
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_map_layers_list_row, parent, false);

            TextView mapLayerNameText = (TextView) convertView.findViewById(R.id.map_layer_name_text);
            FloatingActionButton mapLayerIcon = (FloatingActionButton) convertView.findViewById(R.id.map_layer_icon);

            mapLayerNameText.setText(layer.getRStringIDLayerName());
            mapLayerIcon.setImageResource(layer.getRDrawableIDIcon());

            if (mActiveMapLayerType == layer.getMapLayerType()) {
                mapLayerIcon.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            }
        }

        return convertView;
    }
}