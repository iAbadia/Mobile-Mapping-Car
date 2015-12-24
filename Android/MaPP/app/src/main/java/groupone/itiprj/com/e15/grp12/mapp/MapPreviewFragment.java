package groupone.itiprj.com.e15.grp12.mapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Fragment used for displaying the preview of the map selected
 */
public class MapPreviewFragment extends Fragment {

    TextView mapText;
    ImageView img;
    Context ctx;

    OnMapPreviewListener mCallback;

    public MapPreviewFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_preview, container, false);

        mapText = (TextView) view.findViewById(R.id.text_display);
        img = (ImageView) view.findViewById(R.id.imageView);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ctx = view.getContext();

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onPreviewClicked();
                Log.v("TAG", "Preview Clicked");
            }
        });

        return view;
    }

    public interface OnMapPreviewListener {
        void onPreviewClicked();
    }

    /**
     * Set the image view to the map based on its name.
     */
    public void updateMapView(String map) {

        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/" + map + ".png";
        Log.v("TAG", filepath);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        img.setImageBitmap(bitmap);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);

        mapText.setText("The " + map + " was selected");

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnMapPreviewListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMapPreviewListener");
        }
    }

}