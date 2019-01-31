package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Toolbar Fragment.
 */
public class ToolbarFragment extends Fragment {

//    private ShareActionProvider shareActionProvider;
//    String myDataFromActivity;

    public ToolbarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        if (getActivity() instanceof ChartActivity) {
//            ChartActivity activity = (ChartActivity) getActivity();
//            myDataFromActivity = activity.getMyData();
//        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_toolbar, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        // Hide action icon of current activity
        if (getActivity() instanceof MapsActivity) {
            menu.findItem(R.id.action_map).setVisible(false);
        } else if (getActivity() instanceof ChartActivity) {
            menu.findItem(R.id.action_charts).setVisible(false);
//            menu.findItem(R.id.action_share).setVisible(false);
//            MenuItem menuItem = menu.findItem(R.id.action_share);
//            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//            setShareActionIntent(myDataFromActivity);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                startActivity(new Intent(getActivity(), MapsActivity.class));
                return true;
            case R.id.action_charts:
                startActivity(new Intent(getActivity(), ChartActivity.class));
                return true;
//            case R.id.action_share:
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    private void setShareActionIntent(String text) {
//        Intent i = new Intent(Intent.ACTION_SEND);
//        i.setType("text/plain");
//        i.putExtra(Intent.EXTRA_TEXT,text);
//        shareActionProvider.setShareIntent(i);
//    }
}
