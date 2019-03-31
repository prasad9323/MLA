package com.essentialapps.locationalarm3;


import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;

public class GeoFenceBootReceiver extends BroadcastReceiver{
    Intent ServiceIntent ;
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
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
            String reminder = pref.getString("Reminder","");
            ServiceIntent = new Intent(context, GeoFenceService.class);
            ServiceIntent.putExtra("Latitude",lat);
            ServiceIntent.putExtra("Longitude",lon);
            ServiceIntent.putExtra("City", add);
            ServiceIntent.putExtra("Reminder", reminder);
            ServiceIntent.putExtra("Id",GEOFENCE_ID);
            context.startService(ServiceIntent);
        }
    }
}

