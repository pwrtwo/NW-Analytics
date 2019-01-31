package com.keyboardape.newwestminsteranalyticsapp.datasets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keyboardape.newwestminsteranalyticsapp.R;

/**
 * DataSetAdapter.
 *
 * Used in MainActivity to display download progress.
 */
public class DataSetAdapter extends ArrayAdapter<DataSet> {

    public DataSetAdapter(Context context, DataSet[] dataSets) {
        super(context, 0, dataSets);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DataSet dataSet = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.download_dataset_row, parent, false);

            TextView name = (TextView) convertView.findViewById(R.id.download_name);
            name.setText(dataSet.getRStringIDDataSetName());
        }

        ProgressBar progressbar = (ProgressBar) convertView.findViewById(R.id.download_progressbar);
        ImageView checkmark = (ImageView) convertView.findViewById(R.id.download_checkmark);

        progressbar.setVisibility((dataSet.isUpdating()) ? View.VISIBLE: View.INVISIBLE);
        checkmark.setVisibility((dataSet.isUpToDate()) ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }
}