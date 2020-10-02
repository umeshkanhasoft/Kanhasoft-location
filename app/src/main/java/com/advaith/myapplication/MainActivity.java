package com.advaith.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.kanhasoft.locationtracker.retro.responce.LocationResponce;
import com.kanhasoft.locationtracker.services.OnLocationApiResponceError;

public class MainActivity extends AppCompatActivity {
    com.kanhasoft.locationtracker.KanhasoftLocationTracker kanhasoftLocationTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kanhasoftLocationTracker = new com.kanhasoft.locationtracker.KanhasoftLocationTracker("97929ab0-e040-4836-9fe4-2a98f0e259a9", new OnLocationApiResponceError() {
            @Override
            public void onLocationSucess(LocationResponce locationResponce) {
                Log.e("<><>Main sucess ", locationResponce.getMessage());
            }

            @Override
            public void onError(String error) {
                Log.e("<><>Main error ", error);
            }
        });
        kanhasoftLocationTracker.bindLocation(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (kanhasoftLocationTracker != null)
            kanhasoftLocationTracker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}