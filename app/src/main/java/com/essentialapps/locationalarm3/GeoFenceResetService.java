package com.essentialapps.locationalarm3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.os.SystemClock.elapsedRealtime;

/**
 * Created by Shubham on 8/3/2017.
 */

public class GeoFenceResetService extends Service {
    Intent ServiceIntent;
    boolean mServiceBound = false;
    GeoFenceService geoFenceService;
    Intent geoServiceIntent, geoResetServiceIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        showToast("Stopped");
        geoServiceIntent = new Intent(this, GeoFenceService.class);
        bindService(geoServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmService.set(ELAPSED_REALTIME, elapsedRealtime() + 1000,
                restartServicePendingIntent);
        showToast("Restarted");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",0);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.putBoolean("key_vibrate", true);
        editor.commit();

        String s1 = pref.getString("Current alert", "None");
        if(s1.equals("None"))
        {
        }
        else
        {
            String GEOFENCE_ID = "myGeofenceID";
            String lat = pref.getString("lat1", null);
            String lon = pref.getString("lon1", null);
            String add = pref.getString("City", null);
            Double latD,lonD;
            latD = Double.valueOf(lat);
            lonD = Double.valueOf(lon);
            String reminder = pref.getString("Reminder","");
            ServiceIntent = new Intent(this, GeoFenceService.class);
            ServiceIntent.putExtra("Latitude",latD);
            ServiceIntent.putExtra("Longitude",lonD);
            ServiceIntent.putExtra("City", add);
            ServiceIntent.putExtra("Reminder", reminder);
            ServiceIntent.putExtra("Id",GEOFENCE_ID);
            startService(ServiceIntent);
        }
        this.stopSelf();
    }
    public void showToast(String string) {
        //Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound  = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GeoFenceService.MyBinder myBinder = (GeoFenceService.MyBinder) service;
            geoFenceService = myBinder.getService();
            mServiceBound = true;
        }
    };

    private void getPermissions() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
        }
    }
}
