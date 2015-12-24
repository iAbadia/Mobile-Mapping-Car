package groupone.itiprj.com.e15.grp12.mapp;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity containing information about the app
 */
public class AboutActivity extends Activity implements View.OnClickListener {

    Button backB, whaaaat;
    TextView aboutTitle;
    TextView aboutContent;
    RelativeLayout bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        backB = (Button) findViewById(R.id.button_back);
        whaaaat = (Button) findViewById(R.id.whaaaat);
        whaaaat.setOnClickListener(this);
        backB.setOnClickListener(this);
        aboutTitle = (TextView) findViewById(R.id.about_title);
        aboutTitle.setText(Html.fromHtml(getString(R.string.about_title)));
        aboutContent = (TextView) findViewById(R.id.about_content);
        aboutContent.setText(Html.fromHtml(getString(R.string.about_content)));
        bg = (RelativeLayout) findViewById(R.id.bg);
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
                            // | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            // | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.whaaaat:
                whaaaat();
                break;
        }
    }

    private void whaaaat() {
        whaaaat.setOnClickListener(null);
        MediaPlayer whaaaatPlayer = MediaPlayer.create(this, R.raw.whaaaatsound);
        whaaaatPlayer.start();
        Toast.makeText(this, "NANANANANANNANANANANANANA", Toast.LENGTH_LONG).show();
        bg.setBackgroundResource(R.drawable.bgwhaaaat);
    }
}
