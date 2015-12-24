package groupone.itiprj.com.e15.grp12.mapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.List;

/**
 * Fragment used for displaying the list of the maps saved in the database
 */
public class MapListFragment extends Fragment {

    OnMapListListener mCallback;
    MapDataSource datasource;
    ListView list;
    List<Map> maps;

    public MapListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_list, container, false);

        //Opening database
        datasource = new MapDataSource(getActivity());
        datasource.open();

        maps = datasource.getAllMaps();

        list = (ListView) view.findViewById(R.id.list);

        //Creating the arrayAdapter
        ArrayAdapter<Map> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, maps);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                mCallback.onMapSelected(maps.get(arg2).getPath());
            }

        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Warning");
                alert.setMessage("Do you really want to delete this map ?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Deleting the map in the database
                        datasource.deleteMap(maps.get(arg2).getId());

                        //Deleting the picture of the map in the sd card.
                        String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                        File f = new File(filename + "/" + maps.get(arg2).getPath() + ".png");
                        Log.v("filename", filename + "/" + maps.get(arg2).getPath() + ".png");
                        f.delete();

                        // Refreshing the simple_list_item
                        maps = datasource.getAllMaps();

                        ArrayAdapter<Map> adapter = (new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, maps));
                        list.setAdapter(adapter);


                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                alert.show();
                return false;
            }
        });
        return view;
    }

    public interface OnMapListListener {
        void onMapSelected(String map);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnMapListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMapListListener");
        }
    }
}
