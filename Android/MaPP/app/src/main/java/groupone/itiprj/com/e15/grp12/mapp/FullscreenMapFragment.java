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

/**
 * Fragment used for displaying the map selected in fullscreen
 */
public class FullscreenMapFragment extends Fragment {

    ImageView imgFullscreen;
    Context ctx;
    OnFullscreenMapListener mCallback;

    public FullscreenMapFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreen_map, container, false);


        imgFullscreen = (ImageView) view.findViewById(R.id.imageView);
        ctx = view.getContext();

        //Add a listener to the image View
        imgFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onScreenClicked();
                Log.v("TAG", "Fullscreen map Clicked");
                imgFullscreen.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        });

        return view;
    }

    public interface OnFullscreenMapListener {
        void onScreenClicked();

    }

    //Set the image view to the map based on its name.
    public void updateMapView(String map) {


        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/" + map + ".png";
        Log.v("TAG", filepath);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        imgFullscreen.setImageBitmap(bitmap);
        imgFullscreen.setScaleType(ImageView.ScaleType.FIT_CENTER);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnFullscreenMapListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFullscreenMapListener");
        }
    }

}
