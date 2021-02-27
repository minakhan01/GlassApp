package com.example.basic;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.loopj.android.http.*;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera = null;
    private StorageReference mStorageRef;
    private Context cameraViewContext;

    public CameraView(Context context) {
        super(context);
        cameraViewContext = context;
        final SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        camera = Camera.open();
//
//        // Set the Hotfix for Google Glass
//        setCameraParameters(camera);

//        // Show the Camera display
//        try {
//            camera.setPreviewDisplay(holder);
//        } catch (Exception e) {
//            releaseCamera();
//        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Start the preview for surfaceChanged
//        if (camera != null) {
//            camera.startPreview();
//        }
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
                    timeStamp + ".jpg");
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


    public void takePicture() {
        Camera.PictureCallback mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Camera View", "onpicturetakencalled");
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//                camera.setPreviewDisplay(cameraView);
                if (pictureFile == null){
                    Log.d("Camera View", "Error creating media file, check storage permissions");
                    return;
                }
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                if (!isConnectedToInternet()) {
                    Log.d("CAMERA VIEW", "NOT CONNECTED");
                    try {
                        FileOutputStream fos;
                        fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                    Log.d("Camera View", "write file");
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    Log.d("Camera View", "mStorageRef "+mStorageRef.getPath());
                    Log.d("Camera View", "getAbsolutePath: "+ pictureFile.getAbsolutePath());
                    StorageReference pictureRef = mStorageRef.child("glass_0/"+timeStamp+".jpg");
                    Log.d("Camera View", "pictureRef "+pictureRef.toString());
                    UploadTask uploadTask = pictureRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Log.d("Camera View","Picture Fail");
                            try {
                                FileOutputStream fos;
                                fos = new FileOutputStream(pictureFile);
                                fos.write(data);
                                fos.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Log.d("Camera View", "write file");
                            camera.stopPreview();
                            camera.release();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                            Log.d("Camera View","Picture Success "+ taskSnapshot.getMetadata());
                            pictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadPhotoUrl) {
                                    //Now play with downloadPhotoUrl
                                    //Store data into Firebase Realtime Database
                                    AsyncHttpClient client = new AsyncHttpClient();
                                    RequestParams params = new RequestParams();
                                    params.put("id", "603a313e7695b1ef9e43101d");
                                    params.put("url", downloadPhotoUrl.toString());
                                    client.post("https://thepallab.com/api/glass/addpic",params, new AsyncHttpResponseHandler() {

                                        @Override
                                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {

                                        }

                                        @Override
                                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                                        }


                                    });
                                }
                            });

                            camera.stopPreview();
                            camera.release();
                        }
                    });
//                } catch (FileNotFoundException e) {
//                    Log.d("Camera View", "File not found: " + e.getMessage());
//                } catch (IOException e) {
//                    Log.d("Camera View", "Error accessing file: " + e.getMessage());
//                }



            }
        };
        try {
            Log.d("Camera View", "set preview display, start preview and take picture");
//            releaseCamera();
            camera = Camera.open();
            // Set the Hotfix for Google Glass
            setCameraParameters(camera);
            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();
            camera.takePicture(null, null, mPicture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) cameraViewContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        Log.d("CAMERA_VIEW", "isconnected: "+ isConnected);
        return isConnected;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Do not hold the camera during surfaceDestroyed - view should be gone
        releaseCamera();
    }

    /**
     * Important HotFix for Google Glass (post-XE11) update
     *
     * @param camera Object
     */
    public void setCameraParameters(Camera camera) {
        if (camera != null) {
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
}

