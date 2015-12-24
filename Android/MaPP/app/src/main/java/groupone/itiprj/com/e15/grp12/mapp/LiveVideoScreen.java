package groupone.itiprj.com.e15.grp12.mapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Activity for car steering and recording
 */
public class LiveVideoScreen extends Activity implements View.OnClickListener, View.OnTouchListener {

    static final String CONNECTED_ACTION = "CONNECTED";
    static final String DATAEND_ACTION = "DATAEND";
    String sAddr, cIp;
    int sX, sY, cPort;
    WebView webVideo;
    FrameLayout bg;
    ImageButton left, right, up, down, rec, maps, stop;
    ProgressBar pBar;
    TextView noVideoSource;
    boolean mBound = false, connectedToArduino = false;
    ArduinoConnection mService = null;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ArduinoConnection.ArduinoConnectionBinder binder = (ArduinoConnection.ArduinoConnectionBinder) service;
            mService = binder.getService();
            mBound = true;
            callbackBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    MapDataSource dataSource;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == CONNECTED_ACTION) {
                if (intent.getBooleanExtra(CONNECTED_ACTION, false)) {
                    rec.setImageResource(R.drawable.recoff);
                    rec.setOnClickListener(LiveVideoScreen.this);
                    connectedToArduino = true;
                } else {
                    rec.setImageResource(R.drawable.recdisconnected);
                    rec.setOnClickListener(null);
                }
            } else if (intent.getAction() == DATAEND_ACTION) {
                rec.setImageResource(R.drawable.recoff);
                // Enable maps button
                maps.setOnTouchListener(LiveVideoScreen.this);
                maps.setAlpha((float) 1);
                // Enable steer buttons
                up.setOnTouchListener(LiveVideoScreen.this);
                up.setAlpha((float) 1);
                down.setOnTouchListener(LiveVideoScreen.this);
                down.setAlpha((float) 1);
                left.setOnTouchListener(LiveVideoScreen.this);
                left.setAlpha((float) 1);
                right.setOnTouchListener(LiveVideoScreen.this);
                right.setAlpha((float) 1);
                rec.setTag(false);
            }
        }
    };
    IntentFilter bReceiverFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video_screen);
        // Open DB
        dataSource = new MapDataSource(this);
        dataSource.open();
        // UI Elements
        left = (ImageButton) findViewById(R.id.left);
        right = (ImageButton) findViewById(R.id.right);
        up = (ImageButton) findViewById(R.id.up);
        down = (ImageButton) findViewById(R.id.down);
        rec = (ImageButton) findViewById(R.id.rec);
        rec.setTag(false);
        maps = (ImageButton) findViewById(R.id.maps);
        stop = (ImageButton) findViewById(R.id.stop);
        webVideo = (WebView) findViewById(R.id.webVideo);
        pBar = (ProgressBar) findViewById(R.id.progressBar);
        bg = (FrameLayout) findViewById(R.id.bg);
        noVideoSource = (TextView) findViewById(R.id.noVideoSource);
        bg.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // UI Buttons listeners
        left.setOnTouchListener(this);
        right.setOnTouchListener(this);
        up.setOnTouchListener(this);
        down.setOnTouchListener(this);
        maps.setOnTouchListener(this);
        stop.setOnTouchListener(this);
        // Get intent data
        Intent intent = getIntent();
        sAddr = intent.getCharSequenceExtra(StartScreen.strURL).toString();
        sX = intent.getIntExtra(StartScreen.strX, 0);
        sY = intent.getIntExtra(StartScreen.strY, 0);
        cIp = intent.getCharSequenceExtra(StartScreen.stIP).toString();
        cPort = intent.getIntExtra(StartScreen.stPORT, 0);
        // Load stream & avoid webview from scrolling
        webVideo.setInitialScale(getScale());
        webVideo.setBackgroundColor(Color.TRANSPARENT);
        webVideo.setOnTouchListener(this);
        webVideo.loadUrl(sAddr);
        webVideo.scrollTo(sX / 2, sY / 2);
        webVideo.setVerticalScrollBarEnabled(false);
        webVideo.setHorizontalScrollBarEnabled(false);
        // Set WebViewClient for error handling and progress bar management
        webVideo.setWebViewClient(new WebViewClient() {
            boolean timeout = true;

            public void onPageFinished(WebView view, String url) {
                pBar.setVisibility(ProgressBar.GONE);
                timeout = false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (timeout) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    webVideo.setVisibility(WebView.GONE);
                                    pBar.setVisibility(ProgressBar.GONE);
                                    noVideoSource.setText("NO VIDEO FEED AVAILABLE");
                                }
                            });
                        }
                    }
                }).start();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                webVideo.setVisibility(WebView.GONE);
                pBar.setVisibility(ProgressBar.GONE);
                noVideoSource.setText("NO VIDEO FEED AVAILABLE");
            }


        });
        // Broadcast receiver
        bReceiverFilter = new IntentFilter();
        bReceiverFilter.addAction(CONNECTED_ACTION);
        bReceiverFilter.addAction(DATAEND_ACTION);
        bReceiverFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcastReceiver, bReceiverFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Service created on ConnectScreen activity, now it only binds to it
        Intent intent = new Intent(this, ArduinoConnection.class);
        intent.putExtra(StartScreen.stIP, cIp);
        intent.putExtra(StartScreen.stPORT, cPort);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBound && !connectedToArduino) {
            mService.startConnection();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unbind from the service
        if (mBound) {
            // Since we binded from the Application context, we must unbind it same way
            getApplicationContext().unbindService(mConnection);
            mBound = false;
        }
        if (connectedToArduino) {
            mService.stopConnection();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            webVideo.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * Get right scale for WebView
     *
     * @return Scale to apply
     */
    private int getScale() {
        DisplayMetrics size = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(size);
        Double val = size.widthPixels / (sX + 0.0);
        val = val * 100;
        return val.intValue();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rec:
                // Change icon
                if ((Boolean) v.getTag()) {
                    rec.setImageResource(R.drawable.recoff);
                    // Enable maps button
                    maps.setOnTouchListener(this);
                    maps.setAlpha((float) 1);
                    // Enable steer buttons
                    up.setOnTouchListener(this);
                    up.setAlpha((float) 1);
                    down.setOnTouchListener(this);
                    down.setAlpha((float) 1);
                    left.setOnTouchListener(this);
                    left.setAlpha((float) 1);
                    right.setOnTouchListener(this);
                    right.setAlpha((float) 1);
                    rec.setTag(false);
                    mService.sendInstruction(CarInstruction.STOPSENSORDATA);
                } else {
                    // Arduino service will launch an async task to retrieve sensor data
                    rec.setImageResource(R.drawable.recon);
                    // Disable maps button
                    maps.setOnTouchListener(null);
                    maps.setAlpha((float) 0.5);
                    // Disable steering buttons
                    up.setOnTouchListener(null);
                    up.setAlpha((float) 0.5);
                    down.setOnTouchListener(null);
                    down.setAlpha((float) 0.5);
                    left.setOnTouchListener(null);
                    left.setAlpha((float) 0.5);
                    right.setOnTouchListener(null);
                    right.setAlpha((float) 0.5);
                    rec.setTag(true);
                    mService.sendInstruction(CarInstruction.SENSORDATA);
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            switch (v.getId()) {
                case R.id.webVideo:
                    // Disable scrolling
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                case R.id.maps:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        maps.setAlpha((float) 0.7);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        maps.setAlpha((float) 1);
                        Intent intent = new Intent(this, MapList.class);
                        startActivity(intent);
                    }
                    break;
                case R.id.stop:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        stop.setAlpha((float) 0.7);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        stop.setAlpha((float) 1);
                        mService.sendInstruction(CarInstruction.KILL);
                        finish();
                    }
                    break;
                case R.id.left:
                /*Steer car*/
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        left.setAlpha((float) 0.7);
                        right.setOnTouchListener(null);
                        mService.sendInstruction(CarInstruction.LEFT);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mService.sendInstruction(CarInstruction.STRAIGHT);
                        right.setOnTouchListener(this);
                        left.setAlpha((float) 1);
                    }
                    break;
                case R.id.right:
                /*Steer car*/
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        right.setAlpha((float) 0.7);
                        left.setOnTouchListener(null);
                        mService.sendInstruction(CarInstruction.RIGHT);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mService.sendInstruction(CarInstruction.STRAIGHT);
                        left.setOnTouchListener(this);
                        right.setAlpha((float) 1);
                    }
                    break;
                case R.id.up:
                /*Steer car*/
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        up.setAlpha((float) 0.7);
                        down.setOnTouchListener(null);
                        mService.sendInstruction(CarInstruction.FORWARD);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mService.sendInstruction(CarInstruction.STOP);
                        down.setOnTouchListener(this);
                        up.setAlpha((float) 1);
                    }
                    break;
                case R.id.down:
                /*Steer car*/
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        down.setAlpha((float) 0.7);
                        up.setOnTouchListener(null);
                        mService.sendInstruction(CarInstruction.BACKWARD);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mService.sendInstruction(CarInstruction.STOP);
                        up.setOnTouchListener(this);
                        down.setAlpha((float) 1);
                    }
                    break;
            }
        } catch (NullPointerException e) {
            Log.d("BUTTONPRESS", "Button pressed when no arduino connection up");
        }
        return false;
    }

    public void callbackBound() {
        // Start connection to arduino
        mService.startConnection();
    }
}
