package com.example.basic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.google.android.glass.content.Intents;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private static final int TAKE_PICTURE_REQUEST = 1;
    private static final int TAKE_VIDEO_REQUEST = 2;
    private GestureDetector mGestureDetector = null;
    private CameraView cameraView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Main Activity", "onCreate");

        // Initiate CameraView
        cameraView = new CameraView(this);

        // Turn on Gestures
        mGestureDetector = createGestureDetector(this);

        // Set the view
        setContentView(R.layout.activity_main);

        startService(new Intent(this, CameraService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // releaseCamera();
        // Do not hold the camera during onResume
        if (cameraView != null) {
            cameraView.releaseCamera();
        }

        // Set the view
        setContentView(cameraView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // releaseCamera();

        // Do not hold the camera during onPause
        if (cameraView != null) {
            cameraView.releaseCamera();
        }
    }

    /**
     * Gesture detection for fingers on the Glass
     */
    private GestureDetector createGestureDetector(Context context) {
        final GestureDetector gestureDetector = new GestureDetector(context);

        // Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                // Make sure view is initiated
                if (cameraView != null) {
                    // Tap with a single finger for photo
                    if (gesture == Gesture.TAP) {
                        Log.d("Main Activity", "tapped");
                        // Camera.PictureCallback mPicture = new Camera.PictureCallback() {
                        //
                        // @Override
                        // public void onPictureTaken(byte[] data, Camera camera) {
                        //
                        // File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        // try {
                        // camera.setPreviewDisplay((SurfaceHolder) cameraView);
                        // } catch (IOException e) {
                        // e.printStackTrace();
                        // }
                        // if (pictureFile == null){
                        // Log.d("Main Activity", "Error creating media file, check storage
                        // permissions");
                        // return;
                        // }
                        //
                        // try {
                        // FileOutputStream fos = new FileOutputStream(pictureFile);
                        // fos.write(data);
                        // fos.close();
                        // } catch (FileNotFoundException e) {
                        // Log.d("Main Activity", "File not found: " + e.getMessage());
                        // } catch (IOException e) {
                        // Log.d("Main Activity", "Error accessing file: " + e.getMessage());
                        // }
                        // }
                        // };
                        cameraView.takePicture();
                        // }
                        // mCamera = Camera.open();
                        //
                        // try {
                        // FileOutputStream fos = new FileOutputStream(pictureFile);
                        // fos.write(data);
                        // fos.close();
                        // } catch (FileNotFoundException e) {
                        // Log.d("Main Activity", "File not found: " + e.getMessage());
                        // } catch (IOException e) {
                        // Log.d("Main Activity", "Error accessing file: " + e.getMessage());
                        // }
                        // }
                        // };
                        // camera.takePicture(null, null, mPicture);

                        // startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                        // TAKE_PICTURE_REQUEST);

                        // mCamera.startPreview();
                        // Camera.PictureCallback mPicture = new Camera.PictureCallback() {
                        //
                        // @Override
                        // public void onPictureTaken(byte[] data, Camera camera) {
                        //
                        // File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        // if (pictureFile == null){
                        // Log.d("Main Activity", "Error creating media file, check storage
                        // permissions");
                        // return;
                        // }
                        return true;
                    } else if (gesture == Gesture.TWO_TAP) {
                        Log.d("Main Activity", "TWO tapped");
                        // Tap with 2 fingers for video
                        startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE), TAKE_VIDEO_REQUEST);

                        return true;
                    }
                }

                return false;
            }
        });

        return gestureDetector;
    }
    // private Camera mCamera;

    // public static final int MEDIA_TYPE_IMAGE = 1;
    // public static final int MEDIA_TYPE_VIDEO = 2;
    //
    // /** Create a file Uri for saving an image or video */
    // private static Uri getOutputMediaFileUri(int type){
    // return Uri.fromFile(getOutputMediaFile(type));
    // }
    //
    // /** Create a File for saving an image or video */
    // private static File getOutputMediaFile(int type){
    // // To be safe, you should check that the SDCard is mounted
    // // using Environment.getExternalStorageState() before doing this.
    //
    // File mediaStorageDir = new
    // File(Environment.getExternalStoragePublicDirectory(
    // Environment.DIRECTORY_PICTURES), "MyCameraApp");
    // // This location works best if you want the created images to be shared
    // // between applications and persist after your app has been uninstalled.
    //
    // // Create the storage directory if it does not exist
    // if (! mediaStorageDir.exists()){
    // if (! mediaStorageDir.mkdirs()){
    // Log.d("MyCameraApp", "failed to create directory");
    // return null;
    // }
    // }
    //
    // // Create a media file name
    // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new
    // Date());
    // File mediaFile;
    // if (type == MEDIA_TYPE_IMAGE){
    // mediaFile = new File(mediaStorageDir.getPath() + File.separator +
    // "IMG_"+ timeStamp + ".jpg");
    // } else if(type == MEDIA_TYPE_VIDEO) {
    // mediaFile = new File(mediaStorageDir.getPath() + File.separator +
    // "VID_"+ timeStamp + ".mp4");
    // } else {
    // return null;
    // }
    //
    // return mediaFile;
    // }

    // private void releaseCamera(){
    // if (mCamera != null){
    // mCamera.release(); // release the camera for other applications
    // mCamera = null;
    // }
    // }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // Send generic motion events to the gesture detector
        Log.d("Main Activity", "onGenericMotionEvent");
        return mGestureDetector != null && mGestureDetector.onMotionEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle photos
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            String picturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
            processPictureWhenReady(picturePath);
        }

        // Handle videos
        if (requestCode == TAKE_VIDEO_REQUEST && resultCode == RESULT_OK) {
            String picturePath = data.getStringExtra(Intents.EXTRA_VIDEO_FILE_PATH);
            processPictureWhenReady(picturePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Process picture - from example GDK
     */
    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).
            final File parentDirectory = pictureFile.getParentFile();
            final FileObserver observer = new FileObserver(parentDirectory.getPath()) {
                // Protect against additional pending events after CLOSE_WRITE is
                // handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        final File affectedFile = new File(parentDirectory, path);
                        isFileWritten = (event == FileObserver.CLOSE_WRITE && affectedFile.equals(pictureFile));

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Stop the preview and release the camera.
        // Execute your logic as quickly as possible
        // so the capture happens quickly.
        return keyCode != KeyEvent.KEYCODE_CAMERA && super.onKeyDown(keyCode, event);
    }
}
