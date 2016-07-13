package trevx.util;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import trevx.MainActivity;
import trevx.Musicplayer.MusicService;

/**
 * Created by ptk on 6/22/16.
 */
public class Shaked_Device implements SensorListener {

    public static Shaked_Device shaked_device=new Shaked_Device();
    // For shake motion detection.
    private SensorManager sensorMgr;
    private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;
    Context context;


    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                x = values[SensorManager.DATA_X];
                y = values[SensorManager.DATA_Y];
                z = values[SensorManager.DATA_Z];

                if(Round(x,4)>10.0000){
                    Log.d("sensor", "X Right axis: " + x);
                //    Toast.makeText(this, "Right shake detected", Toast.LENGTH_SHORT).show();
                    Snackbar.make(MainActivity.layout,"Start Playing Next Song",Snackbar.LENGTH_SHORT).show();
                    if(MusicService.mPlayer!=null) {
                        Intent i2 = new Intent(MusicService.ACTION_SKIP);
                        i2.setPackage(context.getPackageName());
                        context.startService(i2);
                    }
                }
                else if(Round(x,4)<-10.0000){
                    Log.d("sensor", "X Left axis: " + x);
                //    Toast.makeText(this, "Left shake detected", Toast.LENGTH_SHORT).show();
                    Snackbar.make(MainActivity.layout,"Start PLaying Previous Song",Snackbar.LENGTH_SHORT).show();
                    if(MusicService.mPlayer!=null) {
                        Intent i2 = new Intent(MusicService.ACTION_REWIND);
                        i2.setPackage(context.getPackageName());
                        context.startService(i2);
                    }
                }

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                // Log.d("sensor", "diff: " + diffTime + " - speed: " + speed);
                if (speed > SHAKE_THRESHOLD) {
                    //Log.d("sensor", "shake detected w/ speed: " + speed);
                    //Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                    Snackbar.make(MainActivity.layout,"Shaked Detecting to Left",Snackbar.LENGTH_SHORT).show();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    public void start_Detection(Context context){

        // start motion detection
        this.context=context;
        sensorMgr = (SensorManager) context.getApplicationContext().getSystemService(context.SENSOR_SERVICE);
        boolean accelSupported = sensorMgr.registerListener(this,
                SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);

        if (!accelSupported) {
            // on accelerometer on this device
            sensorMgr.unregisterListener(this,
                    SensorManager.SENSOR_ACCELEROMETER);
        }
    }

    public void stop_Detection()
    {
        if (sensorMgr != null) {
            sensorMgr.unregisterListener(this,
                    SensorManager.SENSOR_ACCELEROMETER);
            sensorMgr = null;
        }
    }
    @Override
    public void onAccuracyChanged(int i, int i1) {

    }


    public static float Round(float Rval, int Rpl) {
        float p = (float)Math.pow(10,Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float)tmp/p;
    }
}
