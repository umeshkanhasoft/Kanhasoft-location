package com.kanhasoft.locationtracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.kanhasoft.locationtracker.R;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;


/**
 * Created by devdeeds.com on 27-09-2017.
 */

public class LocationMonitoringService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    LocalBroadcastManager broadcastReceiver;

    private final String TAG = LocationMonitoringService.class.getSimpleName();
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;
    Location currentLocation;
    GetLocation GetLocation;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        startServiceOreoCondition();
        broadcastReceiver = LocalBroadcastManager.getInstance(this);
        Log.i(TAG, "Service is Created");

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        googleApiClient.connect();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "service is started");
        GetLocation = new GetLocation(this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        googleApiClient.connect();

        if (googleApiClient.isConnected()) {
            requestLocationUpdates();
//            if (currentLocation != null) {
//                callMyBrodCast(currentLocation);
//            } else {

            if (GetLocation == null) {
                GetLocation = new GetLocation(this);
            }
            if (GetLocation.canGetLocation()) {
                double lat = GetLocation.getLatitude();
                double lon = GetLocation.getLongitude();
                callMyBrodCast(lat,lon);
            } else {
                GetLocation.showGpsAlertDialog(this);
            }
//            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Service is Started Fused onConnected");
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Service is Started Fused onLocationChanged");
        if (location != null) {
            callMyBrodCast(location.getLatitude(),location.getLongitude());
        }
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } else {
            googleApiClient.connect();
        }
//        if (currentLocation != null) {
//            Log.i(TAG, "" + currentLocation.getLatitude() + " , " + currentLocation.getLongitude());
//            callMyBrodCast(currentLocation);
//        } else {
        if (GetLocation == null) {
            GetLocation = new GetLocation(this);
        }
        if (GetLocation.canGetLocation()) {
            double lat = GetLocation.getLatitude();
            double lon = GetLocation.getLongitude();
            callMyBrodCast(lat,lon);
        } else {
            GetLocation.showGpsAlertDialog(this);
        }
//        }
    }

    private void callMyBrodCast(Double lat,Double lon) {
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lon);
        broadcastReceiver.sendBroadcast(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG, "service is Binded");
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        requestLocationUpdates();
        return mBinder;
    }


    public class LocalBinder extends Binder {
        public LocationMonitoringService getService() {
            return LocationMonitoringService.this;
        }
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }


    public void checkLocationSetting(final AppCompatActivity activity) {
        //        check Settings is enabled/disabled
        Log.i(TAG, " check Settings is enabled/disabled from " + activity.getLocalClassName());
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
//                             in onActivityResult().
                            status.startResolutionForResult(activity, 5000);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }

            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startServiceOreoCondition() {
        if (Build.VERSION.SDK_INT >= 26) {


            String CHANNEL_ID = "my_service";
            String CHANNEL_NAME = "My Background Service";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.drawable.common_google_signin_btn_icon_dark).setPriority(PRIORITY_MIN).build();

            startForeground(101, notification);
        }
    }
}