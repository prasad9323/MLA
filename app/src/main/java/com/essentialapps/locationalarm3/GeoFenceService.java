package com.essentialapps.locationalarm3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import android.Manifest;
import android.os.Binder;
import android.util.Log;
import java.util.ArrayList;
import static com.google.android.gms.location.LocationServices.API;

public class GeoFenceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    protected static final String LOG_TAG = GeoFenceService.class.getName();
    private GoogleApiClient googleApiClient;
    Temp homeActivity;
    String id,Current_alert;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        showToast("On bind");
        buildGoogleApiClient();
        googleApiClient.connect();
        return null;
    }

    public class MyBinder extends Binder {
        GeoFenceService getService() {
            return GeoFenceService.this;
        }
    }

    public void showToast(String string) {
        //Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        showToast("Connected");
        reloadAlert();
    }

    private void reloadAlert() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",0);
        Current_alert = pref.getString("Current alert", "None"); // getting String
        if (!Current_alert.equals("None"))
        {
            String id = "myGeofenceID";
            String lat1 = pref.getString("lat1", null);
            String lon1 = pref.getString("lon1", null);
            Double lat,lon;
            lat = Double.valueOf(lat1);
            lon = Double.valueOf(lon1);
            addGeoFence(lat, lon, id);
            showToast("Reloaded");
            Current_alert = pref.getString("Current alert", "None"); // getting String
            runAsForeground();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        buildGoogleApiClient();
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, String.format("onLocationChanged New Latitude: %s ,Longitude %s", location.getLatitude(), location.getLongitude()));
        showToast("Updated");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        googleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getPermissions();
        showToast("onStartCommand");
        googleApiClient.connect();
        if (intent == null)
        {
            showToast("Intent is null");
        }
        else
        {
            if (googleApiClient.isConnected()) {
                id = intent.getStringExtra("Id");
                Double lat = intent.getDoubleExtra("Latitude", 00.000000);
                Double lon = intent.getDoubleExtra("Longitude", 00.000000);
                showToast(id);
                addGeoFence(lat, lon, id);
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",0);
                Current_alert = pref.getString("Current alert", "None"); // getting String
                runAsForeground();
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void addGeoFence(Double lat, Double lon, String id) {
        getPermissions();
        String s = lat.toString();
        showToast(s);
        try{
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(id)
                    .setCircularRegion(lat, lon, 1000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence).build();
            Intent intent = new Intent(this, GeoFenceIntentReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ;
            if (!googleApiClient.isConnected()) {
                Log.d(LOG_TAG, "GoogleApiClient is not Connected");
                showToast("GoogleApiClient is not Connected");
            } else {
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    Log.d(LOG_TAG, "Successfully add geofencingRequest");
                                    showToast("Successfully added geofence, check logcat for geofence updates");
                                } else {
                                    Log.d(LOG_TAG, "Failed to add geofencingRequest" + status.getStatus());
                                    showToast("Failed to add geofence");
                                }
                            }
                        });
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Location not found, please check the internet and try again!", Toast.LENGTH_LONG).show();
        }
    }

    private void getPermissions() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
        }
    }

    protected synchronized void buildGoogleApiClient() {
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (response != ConnectionResult.SUCCESS)
            GoogleApiAvailability.getInstance().getErrorDialog(homeActivity, response, 1).show();
        else {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(AppIndex.API).build();
        }
    }

    private void runAsForeground() {
        Intent notificationIntent = new Intent(this, Launcher.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Mumbai local alarm")
                .setContentText("Alarm set for "+Current_alert)
                .setContentIntent(pendingIntent).build();
        startForeground(1337, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient.isConnected()) {
            ArrayList<String> geofenceIds = new ArrayList<String>();
            geofenceIds.add(id);
            LocationServices.GeofencingApi.removeGeofences(googleApiClient,geofenceIds);
            googleApiClient.disconnect();
        } else {
            googleApiClient.disconnect();
        }
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1337);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Current alert", "None");
        editor.putString("isrunning", "null"); // Storing string
        editor.commit();
    }
}




