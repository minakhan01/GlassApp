package com.example.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//import androidx.annotation.Nullable;

public class CameraService extends Service {
    private CameraView cameraView = null;
    private Camera camera = null;
    private static final String TAG = CameraService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Initiate CameraView
//        cameraView = new CameraView(this);
        TakeIntervalPicture();
        Log.d("Camera Service", "onStartCommand");
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    int a = 0;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 30000;

    private void TakeIntervalPicture() {
        Log.d("Camera Service", "TakeIntervalPicture");
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                a++;
                Log.d("Camera Service", "count: " + a);
                handler.postDelayed(runnable, delay);
//                if (cameraView != null) {
//                    cameraView.takePicture();
//                }
                takePicture();
            }
        }, delay);

    }

    /**
     * Important HotFix for Google Glass (post-XE11) update
     *
     * @param camera Object
     */
    public void setCameraParameters(Camera camera) {
        if (camera != null) {
            Log.d("Camera Service", "setCameraParameters");
            final Camera.Parameters parameters = camera.getParameters();
            List<int[]> supportedPreviewFpsRanges = parameters.getSupportedPreviewFpsRange();
            int[] minimumPreviewFpsRange = supportedPreviewFpsRanges.get(0);
            parameters.setPreviewFpsRange(minimumPreviewFpsRange[0], minimumPreviewFpsRange[1]);
//            parameters.setPreviewFpsRange(15000, 30000);
            camera.setParameters(parameters);
        }
    }

    /**
     * Release the camera from use
     */
    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


    private void takePicture() {

        Camera.PictureCallback mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d(TAG, "onpicturetakencalled");
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//                camera.setPreviewDisplay(cameraView);
                if (pictureFile == null){
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Log.d(TAG, "write file");
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
                camera.stopPreview();
                camera.release();
            }
        };
        releaseCamera();
        camera = Camera.open();
        // Set the Hotfix for Google Glass
        setCameraParameters(camera);
        if (camera != null) {
            SurfaceView sv = new SurfaceView(this);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        SurfaceHolder sh = sv.getHolder();

        sv.setZOrderOnTop(true);
        sh.setFormat(PixelFormat.TRANSPARENT);

        sh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("Camera Service", "Surface Created");
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                    camera.takePicture(null, null, mPicture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("Camera Service", "Surface Changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("Camera Service", "Surface Destroyed");
                releaseCamera();
            }
        });
        wm.addView(sv, params);
        } else {
        Log.d("Camera Service", "Get Camera from service failed");
    }

    }

    @Override

    // execution of the service will
    // stop on calling this method
    public void onDestroy() {
        Log.d("Camera Service", "onDestroy");
        super.onDestroy();

    }

//    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}