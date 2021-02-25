package com.example.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

//import androidx.annotation.Nullable;

public class CameraService extends Service {
    private CameraView cameraView = null;
    protected PowerManager.WakeLock mWakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Initiate CameraView
        cameraView = new CameraView(this);
        TakeIntervalPicture();
        Log.d("Camera Service", "onStartCommand");
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "basic:wakelock");
        mWakeLock.acquire();
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    int a = 0;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 60*1000;

    private void TakeIntervalPicture() {
        Log.d("Camera Service", "TakeIntervalPicture");
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                a++;
                Log.d("Camera Service", "count: " + a);
               handler.postDelayed(runnable, delay);
                if (cameraView != null) {
                    cameraView.takePicture();
                }
            }
        }, delay);

    }

    @Override

    // execution of the service will
    // stop on calling this method
    public void onDestroy() {
        Log.d("Camera Service", "onDestroy");
        this.mWakeLock.release();
        super.onDestroy();

    }

//    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}