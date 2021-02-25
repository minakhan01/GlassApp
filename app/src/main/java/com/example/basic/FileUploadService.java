package com.example.basic;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Service;
import android.content.Intent;
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

    int a = 0;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 10 * 60 * 1000 + 20;

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
                } else {
                    for (int i = 0; i < contents.length; i++) {
                        Log.d("File Upload Service", "upload");
                        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                        Log.d("File Upload Service", "mStorageRef " + mStorageRef.getPath());
                        StorageReference pictureRef = mStorageRef.child(contents[i].getName());
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