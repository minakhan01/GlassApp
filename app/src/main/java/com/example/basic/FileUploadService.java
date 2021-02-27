package com.example.basic;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FileUploadService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileUploadInterval();
        Log.d("File Upload Service", "onStartCommand");
        return START_STICKY;
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        Log.d("CAMERA_VIEW", "isconnected: "+ isConnected);
        return isConnected;
    }

    int a = 0;
    Handler handler = new Handler();
    Runnable runnable;
    // int delay = 10 * 60 * 1000 + 20;
    int delay = 1 * 60 * 1000;

    private void FileUploadInterval() {
        Log.d("File Upload Service", "FileUploadInterval");
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                a++;
                Log.d("File Upload Service", "count: " + a);
                handler.postDelayed(runnable, delay);
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "MyCameraApp");
                File[] contents = directory.listFiles();
                if (contents == null || contents.length == 0) {
                    Log.d("File Upload Service", "No Pictures");
                }
                else if(!isConnectedToInternet()) {
                    Log.d("File Upload Service", "not connected to internet");
                }
                else {
                    for (int i = 0; i < contents.length; i++) {
//                        File fileToDelete = new File(contents[i].getAbsolutePath());
//                        boolean deleted = fileToDelete.delete();
//                        Log.d("File Upload Service", "Picture deleted" + deleted);
                         Log.d("File Upload Service", "upload");
                         StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                         Log.d("File Upload Service", "mStorageRef " + mStorageRef.getPath());
                         StorageReference pictureRef = mStorageRef.child("glass_0/"+contents[i].getName());
                         Log.d("File Upload Service", "pictureRef " + pictureRef.toString());
                         Uri file = Uri.fromFile(new File(contents[i].getAbsolutePath()));
                         UploadTask uploadTask = pictureRef.putFile(file);
                         int finalI = i;
                         uploadTask.addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception exception) {
                                 // Handle unsuccessful uploads
                                 Log.d("File Upload Service", "Picture Fail");

                             }
                         }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                             @Override
                             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                 Log.d("File Upload Service", "Picture Success");
                                 pictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadPhotoUrl) {
                                        //Now play with downloadPhotoUrl
                                        //Store data into Firebase Realtime Database
                                        AsyncHttpClient client = new AsyncHttpClient();
                                        RequestParams params = new RequestParams();
                                        params.put("id", "603a314c7695b1ef9e43101f");
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
                                 File fileToDelete = new File(contents[finalI].getAbsolutePath());
                                 boolean deleted = fileToDelete.delete();
                                 Log.d("File Upload Service", "Picture deleted" + deleted);
                             }
                         });
                    }

                }
            }
        }, delay);

    }

    @Override

    // execution of the service will
    // stop on calling this method
    public void onDestroy() {
        Log.d("File Upload Service", "onDestroy");
        super.onDestroy();

    }

    // @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
