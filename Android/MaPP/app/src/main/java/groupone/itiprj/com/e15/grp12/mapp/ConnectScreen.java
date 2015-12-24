package groupone.itiprj.com.e15.grp12.mapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Activity where user inputs the connection data
 */
public class ConnectScreen extends Activity implements View.OnClickListener {

    FrameLayout bg;
    Button backB, fillDefB, connB;
    EditText sAddr, sX, sY, cIp, cPort;

    public ConnectScreen() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_screen);
        // Buttons and EditTexts
        bg = (FrameLayout) findViewById(R.id.bg);
        backB = (Button) findViewById(R.id.button_back);
        fillDefB = (Button) findViewById(R.id.button_fillDef);
        connB = (Button) findViewById(R.id.button_connect);
        sAddr = (EditText) findViewById(R.id.streamAddr);
        sX = (EditText) findViewById(R.id.xSize);
        sY = (EditText) findViewById(R.id.ySize);
        cIp = (EditText) findViewById(R.id.steerIP);
        cPort = (EditText) findViewById(R.id.steerPort);
        // Set Fullscreen
        bg.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        backB.setOnClickListener(this);
        fillDefB.setOnClickListener(this);
        connB.setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.button_fillDef:
                cIp.setText("192.168.1.129");
                cPort.setText("5556");
                sX.setText("640");
                sY.setText("480");
                sAddr.setText("http://192.168.0.36:8080/video");
                break;
            case R.id.button_connect:
                if (checkFills()) {
                    try {
                        // Send stream BROADCAST_ACTION & arduino BROADCAST_ACTION to start screen
                        Intent broadcastIntentStream = new Intent();
                        broadcastIntentStream.setAction(StartScreen.BROADCAST_ACTION);
                        broadcastIntentStream.putExtra(StartScreen.strX, sX.getText().toString());
                        broadcastIntentStream.putExtra(StartScreen.strY, sY.getText().toString());
                        broadcastIntentStream.putExtra(StartScreen.strURL, sAddr.getText().toString());
                        broadcastIntentStream.putExtra(StartScreen.stIP, cIp.getText().toString());
                        broadcastIntentStream.putExtra(StartScreen.stPORT, cPort.getText().toString());
                        broadcastIntentStream.addCategory(Intent.CATEGORY_DEFAULT);
                        getApplicationContext().sendBroadcast(broadcastIntentStream);
                    } catch (Exception e) {
                        Log.d("BROADCAST", "Couldn't broadcast data");
                    }
                    finish();
                } else {
                    Toast.makeText(this, "Please fill all the gaps", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Check if all the fields are filled
     *
     * @return True if all filled, false else
     */
    private boolean checkFills() {
        boolean go = true;
        if (sAddr.getText().toString().equals("")) {
            go = false;
        }
        if (sX.getText().toString().equals("")) {
            go = false;
        }
        if (sY.getText().toString().equals("")) {
            go = false;
        }
        if (cIp.getText().toString().equals("")) {
            go = false;
        }
        if (cPort.getText().toString().equals("")) {
            go = false;
        }
        return go;
    }
}
