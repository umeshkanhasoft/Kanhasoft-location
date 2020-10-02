package com.kanhasoft.locationtracker;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.kanhasoft.locationtracker.retro.IResult;
import com.kanhasoft.locationtracker.retro.apicall.ApiKanhasoftLocation;
import com.kanhasoft.locationtracker.retro.request.LocationApiRequest;
import com.kanhasoft.locationtracker.retro.responce.LocationResponce;
import com.kanhasoft.locationtracker.services.LocationMonitoringService;
import com.kanhasoft.locationtracker.services.OnLocationApiResponceError;


public class KanhasoftLocationTracker extends AppCompatActivity {
    private static final String TAG = KanhasoftLocationTracker.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    Context context;
    Intent serviceIntent;
    boolean isLocationServiceConnected = false;
    LocationMonitoringService mService;
    OnLocationApiResponceError onLocationGet;
    String aurthantication = null;

    public KanhasoftLocationTracker(String aurthantication, OnLocationApiResponceError onLocationGet) {
        this.aurthantication = aurthantication;
        this.onLocationGet = onLocationGet;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onLocationGet = new OnLocationApiResponceError() {
            @Override
            public void onLocationSucess(LocationResponce locationResponce) {

            }

            @Override
            public void onError(String error) {

            }
        };

    }


    @Override
    public void onResume() {
        super.onResume();
        bindLocation(KanhasoftLocationTracker.this);
    }

    /**
     * Step 1: Check Google Play services
     */
    public void bindLocation(Context contex) {
        this.context = contex;
        serviceIntent = new Intent(contex, LocationMonitoringService.class);

        LocalBroadcastManager.getInstance(contex).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context cnx, Intent intent) {
                        double latitude = intent.getDoubleExtra(LocationMonitoringService.EXTRA_LATITUDE, 0);
                        double longitude = intent.getDoubleExtra(LocationMonitoringService.EXTRA_LONGITUDE, 0);
                        if (!getValue(aurthantication).isEmpty()) {
                            callApi(aurthantication, latitude, longitude);
                        } else {
                            onLocationGet.onError("your authentication token is empty");
                            Log.e(TAG, "your authentication token is empty");
                        }

                    }
                }, new IntentFilter(LocationMonitoringService.ACTION_LOCATION_BROADCAST)
        );

        if (isGooglePlayServicesAvailable()) {
            //Passing null to indicate that it is executing for the first time.
            startStep2(null);
        } else {
            Toast.makeText(contex, R.string.no_google_playservice_available, Toast.LENGTH_LONG).show();
        }
    }

    private void callApi(String aurthantication, double latitude, double longitude) {
        LocationApiRequest locationApiRequest = new LocationApiRequest();
        locationApiRequest.setLatitude(latitude);
        locationApiRequest.setLongitude(longitude);

        ApiKanhasoftLocation apiKanhasoftLocation = new ApiKanhasoftLocation(context, new IResult() {
            @Override
            public void notifySuccessAsObject(String requestType, Object response) {
                LocationResponce locationResponce = (LocationResponce) response;
                if (locationResponce != null && onLocationGet != null) {
                    onLocationGet.onLocationSucess(locationResponce);
                }
            }

            @Override
            public void onError(String message) {
                if (onLocationGet != null) {
                    Log.e(TAG, message);
                    onLocationGet.onError(message);
                }
            }
        });
        apiKanhasoftLocation.execute(locationApiRequest, aurthantication);
    }

    /**
     * Step 2: Check & Prompt Internet connection
     */
    private Boolean startStep2(DialogInterface dialog) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            promptInternetConnect();
            return false;
        }
        if (dialog != null) {
            dialog.dismiss();
        }
        //Yes there is active internet connection. Next check Location is granted by user or not.

        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            startStep3();
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_alert_no_intenet);
        builder.setMessage(R.string.msg_alert_no_internet);

        String positiveText = context.getString(R.string.btn_label_refresh);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Block the Application Execution until user grants the permissions
                        if (startStep2(dialog)) {
                            //Now make sure about location permission.
                            if (checkPermissions()) {
                                //Step 2: Start the Location Monitor Service
                                //Everything is there to start the service.
                                startStep3();
                            } else if (!checkPermissions()) {
                                requestPermissions();
                            }
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Step 3: Start the Location Monitor Service
     */
    public void startStep3() {
        if (!isLocationServiceConnected) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    /**
     * Return the availability of GooglePlayServices
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog((Activity) context, status, 2404).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionState2 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Start permissions requests.
     */
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean shouldProvideRationale2 = ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions((Activity) context,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        /*Snackbar.make(
                findViewById(android.R.id.content),
                context.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(context.getString(actionStringId), listener).show();*/

        Toast.makeText(context, context.getString(mainTextStringId), Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "Permission granted, updates requested, starting location updates");
                startStep3();

            } else {
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.LIBRARY_PACKAGE_NAME, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }


    @Override
    public void onDestroy() {
        //Stop location sharing service to app server.........
        context.stopService(serviceIntent);
        //Ends................................................
        super.onDestroy();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationMonitoringService.LocalBinder binder = (LocationMonitoringService.LocalBinder) service;
            mService = binder.getService();
            mService.checkLocationSetting((AppCompatActivity) context);
            isLocationServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isLocationServiceConnected = false;
        }
    };

    public static String getValue(String text) {
        return (text != null && !text.isEmpty() && !text.equalsIgnoreCase("null")) ? text : "";
    }

}
