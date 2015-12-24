package groupone.itiprj.com.e15.grp12.mapp;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Service started to draw the map, save the bitmap and add it to the DB
 */
public class DrawingService extends IntentService {
    public DrawingService() {
        super("DrawingService");
    }

    Bitmap bitmap;
    Canvas canvas;
    private float maxX = 0, maxY = 0;
    private Paint mPaint = new Paint();
    private float[] mPts;


    @Override
    protected void onHandleIntent(Intent intent) {

        //Opening database
        MapDataSource datasource = new MapDataSource(getApplicationContext());
        datasource.open();
        //Retrieving the data from the sensors.
        String data = datasource.getCurrentSetOfPoints();

        //Parsing the string
        float[][] sensorResult = extractData(data);
        //Calculating the coordinates
        mPts = buildArray(sensorResult);

        //Creating the bitmap and canvas
        bitmap = Bitmap.createBitmap((int) (maxX * 2 + 20.5), (int) (maxY * 2 + 20.5), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.translate(5, 5);

        //Drawing all the points
        Paint paint = mPaint;
        canvas.translate((float) (maxX + 10.5), (float) (maxY + 10.5));
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3);
        canvas.drawPoints(mPts, paint);

        //Saving the path of the newly created picture in the database
        int lastId = datasource.getLastId();
        lastId += 1;
        String mapName = "map" + lastId;
        datasource.createMap(mapName);

        //Saving the newly created picture in the Smartphone's SD card.
        try {
            String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

            File f = new File(filename, mapName + ".png");
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            // Flip bitmap horizontally, needed for DEMO
            Matrix m = new Matrix();
            m.preScale(1, -1);
            Bitmap flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
            flippedBitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
            flippedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * Transform the distance from the sensor into x/y coordinate.
     *
     * @param sensorResult X & Y coordinates
     */
    private float[] buildArray(float[][] sensorResult) {

        mPts = new float[2 * sensorResult.length];
        int j = 0;
        for (int i = 0; i < sensorResult.length - 1; i++) {
            float x = (float) Math.cos(sensorResult[i][1] * (Math.PI / 180)) * sensorResult[i][0];
            float y = (float) Math.sin(sensorResult[i][1] * (Math.PI / 180)) * sensorResult[i][0];
            if (Math.abs(x) < 0.1) {
                mPts[j] = (float) 0.0;
            } else {
                mPts[j] = x;
            }
            if (Math.abs(y) < 0.1) {
                mPts[j + 1] = (float) 0.0;
            } else {
                mPts[j + 1] = y;
            }
            // Calculate Max
            if (Math.abs(x) > maxX) {
                maxX = Math.abs(x);
            }
            if (Math.abs(y) > maxY) {
                maxY = Math.abs(y);
            }
            j = j + 2;
        }
        return mPts;
    }

    /**
     * Parse the string received from the arduino.
     *
     * @param data String received from arduino
     * @return Parsed data
     */
    private float[][] extractData(String data) {
        String[] firstStep = data.split("/", -1);
        float[][] extractedData = new float[firstStep.length][2];
        for (int i = 0; i < firstStep.length - 1; i++) {

            String[] secondStep = firstStep[i].split("-");

            extractedData[i][0] = Float.parseFloat(secondStep[0]);
            extractedData[i][1] = Float.parseFloat(secondStep[1]);
            Log.d("DATAEXTR", secondStep[0] + "-" + secondStep[1]);
        }

        return extractedData;
    }
}