package groupone.itiprj.com.e15.grp12.mapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the methods used for interacting with the database
 */
public class MapDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_PATH};
    private String[] allColumns_points = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_POINTS};

    public MapDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Add a map to the database
    public Map createMap(String path) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PATH, path);

        long insertId = database.insert(MySQLiteHelper.TABLE_MAPS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MAPS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Map newMap = cursorToMap(cursor);
        cursor.close();
        return newMap;
    }

    /**
     * @param data Insert new set of points in the DB
     */
    private void insertNewSetOfPoints(String data) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_POINTS, data);
        Log.v("dataInserted", data);
        database.insert(MySQLiteHelper.TABLE_POINTSARRAY, null,
                values);
    }

    /**
     * @return The current set of points in the DB
     */
    public String getCurrentSetOfPoints() {

        Cursor cursor = database.query(MySQLiteHelper.TABLE_POINTSARRAY,
                allColumns_points, null, null, null, null, null);

        cursor.moveToFirst();

        return cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_POINTS));
    }

    /**
     * @param data Add points to the current set of points in the DB
     */
    public void addSetOfPoints(String data) {

        String currentPoints = getCurrentSetOfPoints();
        Log.v("pointsArray", currentPoints);
        currentPoints += data + "/";

        deleteCurrentSetOfPoints();

        insertNewSetOfPoints(currentPoints);
    }

    /**
     *
     */
    public void resetCurrentSetOfPoints() {

        Cursor cursor = database.query(MySQLiteHelper.TABLE_POINTSARRAY,
                allColumns_points, null, null, null, null, null);


        if (cursor.getCount() == 0) {
            insertNewSetOfPoints("");
        } else {
            database.delete(MySQLiteHelper.TABLE_POINTSARRAY, null, null);
            insertNewSetOfPoints("");
        }

    }

    // Delete a map from the database
    public void deleteMap(long id) {
        database.delete(MySQLiteHelper.TABLE_MAPS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    private void deleteCurrentSetOfPoints() {
        database.delete(MySQLiteHelper.TABLE_POINTSARRAY, null, null);
    }

    //Drop the table (cause bug - only the table is deleted, not the database)
    public void resetTable() {
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteHelper.TABLE_MAPS);
        dbHelper.onCreate(database);
    }

    // Delete all map from the database (the id doesn't seem to be re-initialised)
    public void deleteAll() {
        database.delete(MySQLiteHelper.TABLE_MAPS, null, null);
    }

    //Return all the map in a 2D array with the id and the path of the map.
    public List<Map> getAllMaps() {

        List<Map> maps = new ArrayList<>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_MAPS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Map map = cursorToMap(cursor);
            maps.add(map);
            cursor.moveToNext();
        }
        cursor.close();
        return maps;
    }

    // Return the last id of the database
    public int getLastId() {
        int lastId;
        try {
            Cursor cursor = database.query(MySQLiteHelper.TABLE_MAPS, allColumns, null, null, null, null, null);
            cursor.moveToLast();

            Map map = cursorToMap(cursor);
            lastId = (int) map.getId();
        } catch (CursorIndexOutOfBoundsException e) {
            // Empty DB
            lastId = -1;
        }

        return lastId;
    }

    // Return the map of the current cursor position
    private Map cursorToMap(Cursor cursor) {
        Map map = new Map();
        map.setId(cursor.getLong(0));
        map.setPath(cursor.getString(1));

        return map;
    }

}
