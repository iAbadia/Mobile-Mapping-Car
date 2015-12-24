package groupone.itiprj.com.e15.grp12.mapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Main activity, first shown when opening the app
 */
public class StartScreen extends Activity implements View.OnClickListener {

    FrameLayout bg;
    Button connectB, startRecB, mapListB, exitB, aboutB;
    String streamURL = null, arduIP = null;
    int streamX, streamY, arduPORT;
    public static final String strURL = "URL", strX = "X", strY = "Y", stIP = "ARDUIP", stPORT = "ARDUPORT", BROADCAST_ACTION = "STREAMDATA";

    /**
     * Used to get the connection data from ConnectScreen
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            streamURL = intent.getCharSequenceExtra(strURL).toString();
            streamX = Integer.parseInt(intent.getCharSequenceExtra(strX).toString());
            streamY = Integer.parseInt(intent.getCharSequenceExtra(strY).toString());
            arduIP = intent.getCharSequenceExtra(stIP).toString();
            arduPORT = Integer.parseInt(intent.getCharSequenceExtra(stPORT).toString());
        }
    };
    IntentFilter bReceiverFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start_screen);
        bg = (FrameLayout) findViewById(R.id.bg);
        connectB = (Button) findViewById(R.id.button_connect);
        startRecB = (Button) findViewById(R.id.button_startRec);
        mapListB = (Button) findViewById(R.id.button_mapList);
        exitB = (Button) findViewById(R.id.button_exit);
        aboutB = (Button) findViewById(R.id.button_about);
        bg.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        connectB.setOnClickListener(this);
        exitB.setOnClickListener(this);
        mapListB.setOnClickListener(this);
        startRecB.setOnClickListener(this);
        aboutB.setOnClickListener(this);
        // Receiver filter
        bReceiverFilter = new IntentFilter();
        bReceiverFilter.addAction(BROADCAST_ACTION);
        bReceiverFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcastReceiver, bReceiverFilter);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            bg.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            // | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            // | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_startRec:
                if (arduIP != null) {
                    Intent liveView = new Intent(this, LiveVideoScreen.class);
                    liveView.putExtra(strURL, streamURL);
                    liveView.putExtra(strX, streamX);
                    liveView.putExtra(strY, streamY);
                    liveView.putExtra(stIP, arduIP);
                    liveView.putExtra(stPORT, arduPORT);
                    // Code 2, StartScreen -> LiveVideoScreen
                    startActivityForResult(liveView, 2);
                } else {
                    Toast.makeText(this, "Please set Arduino IP & PORT first", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.button_connect:
                Intent connView = new Intent(this, ConnectScreen.class);
                // Code 0, StartScreen -> ConnectionScreen
                startActivityForResult(connView, 0);
                break;
            case R.id.button_mapList:
                Intent mapListView = new Intent(this, MapList.class);
                // Code 1, StartScreen -> FullscreenMapView
                startActivityForResult(mapListView, 1);
                break;
            case R.id.button_exit:
                finish();
                break;
            case R.id.button_about:
                Intent aboutAct = new Intent(this, AboutActivity.class);
                startActivity(aboutAct);
                break;
        }
    }
}
