package groupone.itiprj.com.e15.grp12.mapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper class
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    //Table names
    public static final String TABLE_MAPS = "maps";
    public static final String TABLE_POINTSARRAY = "points";

    //Common column names
    public static final String COLUMN_ID = "_id";

    //Column names - Points
    public static final String COLUMN_POINTS = "data";

    //column names - Maps
    public static final String COLUMN_PATH = "path";


    private static final String DATABASE_NAME = "maps.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String MAPS_CREATE = "create table "
            + TABLE_MAPS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_PATH
            + " text not null);";

    private static final String POINTSARRAY_CREATE = "create table "
            + TABLE_POINTSARRAY + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_POINTS
            + " );";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        database.execSQL(MAPS_CREATE);
        database.execSQL(POINTSARRAY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old BROADCAST_ACTION");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINTSARRAY);
        onCreate(db);
    }

}
