package groupone.itiprj.com.e15.grp12.mapp;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * Service to communicate with the Mega board
 */
public class ArduinoConnection extends Service {

    public MapDataSource datasource;
    String SERVER_ADDR;
    int SERVER_PORT;
    Socket socketToArduino;
    DataOutputStream toArduino;
    BufferedReader fromArduino;
    public static boolean recording = false;
    public static Semaphore semaphore;

    private final IBinder mBinder = new ArduinoConnectionBinder();

    public class ArduinoConnectionBinder extends Binder {

        public ArduinoConnection getService() {
            return ArduinoConnection.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        this.SERVER_ADDR = intent.getCharSequenceExtra(StartScreen.stIP).toString();
        this.SERVER_PORT = intent.getIntExtra(StartScreen.stPORT, 0);
        semaphore = new Semaphore(1, true);

        datasource = new MapDataSource(getApplicationContext());
        datasource.open();
        datasource.resetCurrentSetOfPoints();

        return mBinder;
    }

    /**
     * Connect to Mega board Server Socket
     */
    public void startConnection() {
        new ConnectTask().execute();
    }

    /**
     * Inform Mega board that connection is finished
     */
    public void stopConnection() {
        sendInstruction(CarInstruction.KILL);
    }

    /**
     * Sends instruction to Mega board
     *
     * @param carInstruction Instruction to send
     */
    public void sendInstruction(CarInstruction carInstruction) {
        if (carInstruction == CarInstruction.STOPSENSORDATA) {
            recording = false;
        } else if (toArduino != null) {
            if (carInstruction == CarInstruction.KILL) {
                recording = false;
            }
            new SendTask(carInstruction).execute();
        }
    }

    /**
     * Send instruction task, also sets TimerTask when recording is activated
     */
    private class SendTask extends AsyncTask<Void, Void, boolean[]> {
        CarInstruction instruction;

        SendTask(CarInstruction carInstruction) {
            instruction = carInstruction;
        }

        @Override
        protected boolean[] doInBackground(Void... params) {
            boolean[] res = new boolean[3];
            res[0] = false;
            res[1] = false;
            res[2] = false;
            // [0] false : steer instruction, true : datarec instruction
            // [1] true : start recording, false : else
            // [2] true : stop service, false : keep working
            try {
                switch (instruction) {
                    case FORWARD:
                        semaphore.acquire();
                        toArduino.writeChar('F');
                        semaphore.release();
                        break;
                    case BACKWARD:
                        semaphore.acquire();
                        toArduino.writeChar('B');
                        semaphore.release();
                        break;
                    case STOP:
                        semaphore.acquire();
                        toArduino.writeChar('S');
                        semaphore.release();
                        break;
                    case RIGHT:
                        semaphore.acquire();
                        toArduino.writeChar('R');
                        semaphore.release();
                        break;
                    case LEFT:
                        semaphore.acquire();
                        toArduino.writeChar('L');
                        semaphore.release();
                        break;
                    case STRAIGHT:
                        semaphore.acquire();
                        toArduino.writeChar('T');
                        semaphore.release();
                        break;
                    case SENSORDATA:
                        res[0] = true;
                        // First set of points received for new map
                        if (!recording) {
                            // Start recording
                            recording = true;
                            datasource.resetCurrentSetOfPoints();
                        }
                        // Runnable in charge of retrieving the points
                        try {
                            // Start retrieving data
                            for (int i = 0; (i < 9) && recording; i++) {
                                // Concurrency control
                                semaphore.acquire();
                                toArduino.writeChar('D');
                                while (!fromArduino.ready()) ;
                                String data = fromArduino.readLine();
                                Log.d("TALKTOARDUINO", data);
                                // Concurrency control
                                semaphore.release();
                                datasource.addSetOfPoints(data);
                            }
                            // Concurrency control
                            semaphore.acquire();
                            // Send stop-sending
                            toArduino.writeChar('U');
                            // Concurrency control
                            semaphore.release();
                            // Inform of data retrieval end, change rec button
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("TALKTOARDUINOERROR", "ERROR TALKING TO ARDUINO");
                        }
                        // Finish canvas, draw it
                        Intent drawingIntent = new Intent(ArduinoConnection.this, DrawingService.class);
                        ArduinoConnection.this.startService(drawingIntent);
                        break;
                    case KILL:
                        semaphore.acquire();
                        if (toArduino != null && fromArduino != null) {
                            toArduino.writeChar('K');
                            if (fromArduino.readLine().equals("K")) {
                                socketToArduino.close();
                                Log.d("ARDUCONN", "Connection Killed");
                            } else {
                                Log.d("ARDUCONN", "Tried to kill connection, arduino wont comply :(");
                            }
                        }
                        semaphore.release();
                        res[2] = true;
                }
            } catch (IOException e) {
                Log.d("DATASEND", "Error sending data :(");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(boolean[] bool) {
            // If Kill instruction, end service
            if (bool[2]) {
                stopSelf();
            } else if (bool[0]) {
                Intent broadcastIntentConnected = new Intent();
                broadcastIntentConnected.setAction(LiveVideoScreen.DATAEND_ACTION);
                broadcastIntentConnected.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntentConnected.putExtra(LiveVideoScreen.DATAEND_ACTION, bool[0]);
                getApplicationContext().sendBroadcast(broadcastIntentConnected);
            }
        }
    }

    /**
     * Connect to Mega board socket task
     */
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;
            for (int i = 0; i < 5 && !success; i++) {
                try {
                    socketToArduino = new Socket(SERVER_ADDR, SERVER_PORT);
                    Log.d("ARDUINOCONN", "Connected to server!");
                    toArduino = new DataOutputStream(socketToArduino.getOutputStream());
                    fromArduino = new BufferedReader(new InputStreamReader(socketToArduino.getInputStream()));
                    success = true;
                } catch (Exception e) {
                    // Retry
                    socketToArduino = null;
                    Log.d("ARDUINOCONN", "Failed to connect, going to sleep and retry.");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e2) {
                        Log.d("ARDUINOCONN", "Failed going to sleep.");
                    }
                }
            }
            return success;
        }

        /**
         * Inform activity if connection was successful
         *
         * @param res Success flag
         */
        @Override
        protected void onPostExecute(Boolean res) {
            Intent broadcastIntentConnected = new Intent();
            broadcastIntentConnected.setAction(LiveVideoScreen.CONNECTED_ACTION);
            broadcastIntentConnected.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntentConnected.putExtra(LiveVideoScreen.CONNECTED_ACTION, res);
            getApplicationContext().sendBroadcast(broadcastIntentConnected);
        }
    }
}
