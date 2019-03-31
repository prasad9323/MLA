package com.essentialapps.locationalarm3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

public class Temp extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    Place place;
    Boolean found_location;
    private ViewPager mViewPager;
    public static FloatingActionButton fab;
    Toolbar toolbar;
    int Req = 1;
    protected static final String LOG_TAG = Temp.class.getName();
    private static String GEOFENCE_ID = "myGeofenceID";
    private GoogleApiClient googleApiClient;
    TabLayout tabLayout;
    Intent geoServiceIntent, geoResetServiceIntent;
    boolean mServiceBound = false;
    GeoFenceService geoFenceService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        geoServiceIntent = new Intent(this, GeoFenceService.class);
        bindService(geoServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        geoResetServiceIntent = new Intent(this, GeoFenceResetService.class);
        startService(geoResetServiceIntent);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlacePicker();
            }
        });
    }

    private void openPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        Intent intent;
        try
        {
            intent = builder.build(Temp.this);
            startActivityForResult(intent,Req);
        }
        catch (GooglePlayServicesRepairableException e)
        {

        }
        catch (GooglePlayServicesNotAvailableException e)
        {

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Req) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "No location selected", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);
                LatLng latlng = place.getLatLng();
                double lat = latlng.latitude;
                double lon = latlng.longitude;
                geoServiceIntent = new Intent(this, GeoFenceService.class);
                geoServiceIntent.putExtra("Latitude",lat);
                geoServiceIntent.putExtra("Longitude",lon);
                geoServiceIntent.putExtra("Id",GEOFENCE_ID);
                startService(geoServiceIntent);
                found_location = true;
                Toast.makeText(getApplicationContext(), "PlacePicker Ok!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

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
