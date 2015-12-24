package groupone.itiprj.com.e15.grp12.mapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Activity hosting the 3 fragments :MapPreviewFragment, FullscreenMapFragment, MapListFragment
 * Manage all the information exchanges between fragments
 */
public class MapList extends Activity implements MapListFragment.OnMapListListener, MapPreviewFragment.OnMapPreviewListener, FullscreenMapFragment.OnFullscreenMapListener, View.OnClickListener {

    FrameLayout bg;
    Button returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);
        returnButton = (Button) findViewById(R.id.return_button);
        returnButton.setOnClickListener(this);
        bg = (FrameLayout) findViewById(R.id.bg);
        bg.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            bg.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    @Override
    public void onMapSelected(String map) {
        MapPreviewFragment displayFragment = (MapPreviewFragment)
                getFragmentManager().findFragmentById(R.id.display_fragment);
        displayFragment.updateMapView(map);
        FullscreenMapFragment fullscreenFragment = (FullscreenMapFragment)
                getFragmentManager().findFragmentById(R.id.fullscreen_fragment);
        fullscreenFragment.updateMapView(map);
    }

    /**
     * Change the FullscreenMapFragment weight to 500 when the preview is clicked.
     */
    @Override
    public void onPreviewClicked() {
        FullscreenMapFragment fullscreenFragment = (FullscreenMapFragment)
                getFragmentManager().findFragmentById(R.id.fullscreen_fragment);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 500;
        fullscreenFragment.getView().setLayoutParams(params);
    }

    /**
     * Change the FullscreenMapFragment weight to 0 when it's clicked.
     */
    @Override
    public void onScreenClicked() {
        FullscreenMapFragment fullscreenFragment = (FullscreenMapFragment)
                getFragmentManager().findFragmentById(R.id.fullscreen_fragment);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 0;
        fullscreenFragment.getView().setLayoutParams(params);
    }


    /**
     * Close the activity when the "back" button is clicked.
     *
     * @param v View
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_button:
                this.finish();
                break;
        }
    }
}
