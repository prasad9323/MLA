package com.essentialapps.locationalarm3;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import android.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

public class SetAlarmActivity extends AppCompatActivity implements OnItemSelectedListener, LocationListener {

    Spinner spinnerRegion, spinnerCity;
    String text,alertcity;
    String reminder,s;
    String lat=null,lon=null;
    int Req = 1;
    EditText et1;
    boolean mServiceBound = false;
    GeoFenceService geoFenceService;
    View v;
    Place place;
    Boolean found_location;
    Integer positionregion;
    Intent geoServiceIntent, geoResetServiceIntent;
    LocationManager locationManager;
    double LatDouble,LonDouble;
    Integer MIN_TIME_BW_UPDATES = 5000,MIN_DISTANCE_CHANGE_FOR_UPDATES = 10,radius = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AdView mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        catch(Exception e)
        {
        }
        geoServiceIntent = new Intent(this, GeoFenceService.class);
        bindService(geoServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        geoResetServiceIntent = new Intent(this, GeoFenceResetService.class);
        startService(geoResetServiceIntent);
        ScrollView scroll = (ScrollView)findViewById(R.id.ScrollView01);
        scroll.scrollTo(0, 0);
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String s9 = pref.getString("positionregion", "0");
        spinnerRegion = (Spinner) findViewById(R.id.spinnerRegion);
        spinnerRegion.setSelection(Integer.parseInt(s9));
        spinnerCity = (Spinner) findViewById(R.id.spinnerCity);
        spinnerCity.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
                                       long arg3) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
                ((TextView) parent.getChildAt(0)).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                spinnerCity.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerRegion.setOnItemSelectedListener(this);
        alertcity = spinnerRegion.getSelectedItem().toString();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isConnectingToInternet();
                if (isConnectingToInternet()==true)
                {
                    overridePendingTransition(R.anim.animation, R.anim.animation2);
                }
                else {
                    Snackbar snackbar = Snackbar.make(view, "No internet!", Snackbar.LENGTH_SHORT).setAction("Enable", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                    Intent i = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                                    startActivity(i);
                                    // Toast.makeText(getApplicationContext(),"Ignore if already enabled,This may be due to provider failure",Toast.LENGTH_LONG).show();
                                    overridePendingTransition(R.anim.animation, R.anim.animation2);
                                }
                            });
                }
                final Snackbar snackbar2 = Snackbar.make(view, "Location is not available!", Snackbar.LENGTH_LONG)
                        .setAction("Enable", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(i);
                                //Toast.makeText(getApplicationContext(),"Ignore if already enabled",Toast.LENGTH_LONG).show();
                                overridePendingTransition(R.anim.animation, R.anim.animation2);
                            }
                        });
                new CountDownTimer(1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        String s = pref.getString("location", "no");
                        if (s.equals("no"))
                        {
                            // snackbar2.show();
                        }
                    }
                }.start();
                onClickStartServie(view);
            }
        });
    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    public void comingsoon(View v)
    {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfLoc = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean statusOfgps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (statusOfLoc||statusOfgps)
        {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            Intent intent;
            try
            {
                intent = builder.build(SetAlarmActivity.this);
                startActivityForResult(intent,Req);
            }
            catch (GooglePlayServicesRepairableException e)
            {

            }
            catch (GooglePlayServicesNotAvailableException e)
            {

            }
        }
        else
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetAlarmActivity.this);
            alertDialogBuilder.setTitle("Location service disabled!");
            alertDialogBuilder.setMessage("");
            alertDialogBuilder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    Intent i2 = new Intent(getBaseContext(), GeoFenceService.class);
                    stopService(i2);
                    Intent i = new Intent(getBaseContext(), GeoFenceResetService.class);
                    stopService(i);
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Req) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == RESULT_OK) {
                try {
                    place = PlacePicker.getPlace(data, this);
                    String add = String.format((String) place.getAddress());
                    et1 = (EditText) findViewById(R.id.et1);
                    reminder = (et1.getText()).toString();
                    LatLng latlng = place.getLatLng();
                    double lat1 = latlng.latitude;
                    double lng1 = latlng.longitude;
                    String lat = String.valueOf(lat1);
                    String lng = String.valueOf(lng1);
                    String GEOFENCE_ID = "myGeofenceID";
                    geoServiceIntent = new Intent(this, GeoFenceService.class);
                    geoServiceIntent.putExtra("Latitude",lat1);
                    geoServiceIntent.putExtra("Longitude",lng1);
                    geoServiceIntent.putExtra("City", add);
                    geoServiceIntent.putExtra("Reminder", reminder);
                    geoServiceIntent.putExtra("Id",GEOFENCE_ID);
                    startService(geoServiceIntent);
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                    Editor editor = pref.edit();
                    editor.putString("lat1", lat);
                    editor.putString("lon1", lng);
                    editor.putString("City", add);
                    editor.putString("Current alert", add);
                    editor.putString("Reminder", reminder);
                    editor.putString("isrunning", "yes"); // Storing string
                    editor.commit();
                    found_location = true;
                    if ( Build.VERSION.SDK_INT >= 23 &&
                            ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED)
                    {
                    }
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_LONG).show();
                    found_location = false;
                }
                if (found_location==true)
                {
                    finish();
                    Toast.makeText(getApplicationContext(), "Alarm set for " + place.getAddress(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.animation, R.anim.animation2);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.animation, R.anim.animation2);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
                               long arg3) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
        parent.getItemAtPosition(pos);
        if (pos == 0) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.All,
                            android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapter);
        }
        else if (pos == 1) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.central,
                            android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapter);
            text = spinnerCity.getSelectedItem().toString();
        } else if (pos == 2) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.western,
                            android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapter);
            text = spinnerCity.getSelectedItem().toString();
        } else if (pos == 3) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.harbour,
                            android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapter);
            text = spinnerCity.getSelectedItem().toString();
        }
        else if (pos == 4) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.transharbour,
                            android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapter);
            text = spinnerCity.getSelectedItem().toString();
        }
        positionregion = spinnerRegion.getSelectedItemPosition();
        s = Integer.toString(positionregion);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",0);
        Editor editor = pref.edit();
        editor.putString("positionregion", s);
        editor.commit();
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public void onClickStartServie(View V)
    {
        alertcity = spinnerCity.getSelectedItem().toString();
        if (alertcity.equals("Airoli"))
        {	lat = "19.158465";
            lon = "72.999360";
        }
        else if (alertcity.equals("Ambernath"))
        {	lat = "19.209993";
            lon = "73.184855";
        }
        else if (alertcity.equals("Ambivli"))
        {	lat = "19.268410";
            lon = "73.172121";
        }
        else if (alertcity.equals("Andheri"))
        {	lat = "19.119824";
            lon = "72.846468";
        }
        else if (alertcity.equals("Asangaon"))
        {	lat = "19.439321";
            lon = "73.307772";
        }
        else if (alertcity.equals("Atgaon"))
        {	lat = "19.503671";
            lon = "73.328182";
        }
        else if (alertcity.equals("Badlapur"))
        {	lat = "19.166789";
            lon = "73.239083";
        }
        else if (alertcity.equals("Bamandongri"))
        {	lat = "18.974386";
            lon = "73.023727";
        }
        else if (alertcity.equals("Bandra"))
        {	lat = "19.054559";
            lon = "72.840837";
        }
        else if (alertcity.equals("Bhandup"))
        {	lat = "19.142400";
            lon = "72.937632";
        }
        else if (alertcity.equals("Bhayandar"))
        {	lat = "19.311218";
            lon = "72.852631";
        }
        else if (alertcity.equals("Bhivpuri Road"))
        {	lat = "18.970625";
            lon = "73.331426";
        }
        else if (alertcity.equals("Bhiwandi"))
        {	lat = "19.268692";
            lon = "73.045813";
        }
        else if (alertcity.equals("Boisar"))
        {	lat = "19.798289";
            lon = "72.761630";
        }
        else if (alertcity.equals("Borivali"))
        {	lat = "19.228680";
            lon = "72.856702";
        }
        else if (alertcity.equals("Byculla"))
        {	lat = "18.978286";
            lon = "72.833845";
        }
        else if (alertcity.equals("CBD Belapur"))
        {	lat = "19.018991";
            lon = "73.039114";
        }
        else if (alertcity.equals("Charni Road"))
        {	lat = "18.951484";
            lon = "72.818777";
        }
        else if (alertcity.equals("Chembur"))
        {	lat = "19.062577";
            lon = "72.901317";
        }
        else if (alertcity.equals("Chinchpokli"))
        {	lat = "18.986794";
            lon = "72.832745";
        }
        else if (alertcity.equals("Chunabhatti"))
        {	lat = "19.051344";
            lon = "72.868951";
        }
        else if (alertcity.equals("Churchgate"))
        {	lat = "18.935109";
            lon = "72.827358";
        }
        else if (alertcity.equals("Cotton Green"))
        {	lat = "18.986437";
            lon = "72.843189";
        }
        else if (alertcity.equals("Currey Road"))
        {	lat = "18.993955";
            lon = "72.832848";
        }
        else if (alertcity.equals("Dadar"))
        {	lat = "19.017928";
            lon = "72.843327";
        }
        else if (alertcity.equals("Dahanu"))
        {	lat = "19.991115";
            lon = "72.743708";
        }
        else if (alertcity.equals("Dahisar"))
        {	lat = "19.250434";
            lon = "72.859223";
        }
        else if (alertcity.equals("Dativali"))
        {	lat = "19.187163";
            lon = "73.058931";
        }
        else if (alertcity.equals("Diva Junction"))
        {	lat = "19.188693";
            lon = "73.042312";
        }
        else if (alertcity.equals("Dockyard Road"))
        {	lat = "18.966295";
            lon = "72.844255";
        }
        else if (alertcity.equals("Dolavli"))
        {	lat = "18.834173";
            lon = "73.320028";
        }
        else if (alertcity.equals("Dombivli"))
        {	lat = "19.217852";
            lon = "73.086457";
        }
        else if (alertcity.equals("Dronagiri"))
        {	lat = "19.021721";
            lon = "73.019100";
        }
        else if (alertcity.equals("Elphinstone Road"))
        {	lat = "19.007534";
            lon = "72.835844";
        }
        else if (alertcity.equals("Ghansoli"))
        {	lat = "19.116350";
            lon = "73.006987";
        }
        else if (alertcity.equals("Ghatkopar"))
        {	lat = "19.085659";
            lon = "72.908065";
        }
        else if (alertcity.equals("Goregaon"))
        {	lat = "19.164634";
            lon = "72.849543";
        }
        else if (alertcity.equals("Govandi"))
        {	lat = "19.055372";
            lon = "72.914988";
        }
        else if (alertcity.equals("Grant Road"))
        {	lat = "18.964025";
            lon = "72.816367";
        }
        else if (alertcity.equals("GTB nagar"))
        {	lat = "19.037908";
            lon = "72.864185";
        }
        else if (alertcity.equals("Jogeshwari"))
        {	lat = "19.136327";
            lon = "72.848898";
        }
        else if (alertcity.equals("Juchandra"))
        {	lat = "19.360243";
            lon = "72.872663";
        }
        else if (alertcity.equals("Juinagar"))
        {	lat = "19.055707";
            lon = "73.018269";
        }
        else if (alertcity.equals("Kalamboli"))
        {	lat = "19.035272";
            lon = "73.114433";
        }
        else if (alertcity.equals("Kalwa"))
        {	lat = "19.195763";
            lon = "72.997166";
        }
        else if (alertcity.equals("Kalyan"))
        {	lat = "19.235175";
            lon = "73.130380";
        }
        else if (alertcity.equals("Kaman Road"))
        {	lat = "19.336681";
            lon = "72.918304";
        }
        else if (alertcity.equals("Kandivali"))
        {	lat = "19.204425";
            lon = "72.851960";
        }
        else if (alertcity.equals("Kanjurmarg"))
        {	lat = "19.128189";
            lon = "72.928027";
        }
        else if (alertcity.equals("Karjat"))
        {	lat = "18.910877";
            lon = "73.320657";
        }
        else if (alertcity.equals("Kasara"))
        {	lat = "19.648344";
            lon = "73.472918";
        }
        else if (alertcity.equals("Kelavli"))
        {	lat = "16.597862";
            lon = "73.653949";
        }
        else if (alertcity.equals("Kelve Road"))
        {	lat = "19.624324";
            lon = "72.791210";
        }
        else if (alertcity.equals("Khadavli"))
        {	lat = "19.356847";
            lon = "73.219054";
        }
        else if (alertcity.equals("Khandeshwar"))
        {	lat = "19.007391";
            lon = "73.094972";
        }
        else if (alertcity.equals("Khar Road"))
        {	lat = "19.069559";
            lon = "72.840168";
        }
        else if (alertcity.equals("Kharbao"))
        {	lat = "19.298248";
            lon = "72.983837";
        }
        else if (alertcity.equals("Khardi"))
        {	lat = "19.580504";
            lon = "73.394131";
        }
        else if (alertcity.equals("Kharghar"))
        {	lat = "19.025914";
            lon = "73.059280";
        }
        else if (alertcity.equals("Khopoli"))
        {	lat = "18.789572";
            lon = "73.344819";
        }
        else if (alertcity.equals("Kings Circle"))
        {	lat = "19.032088";
            lon = "72.857418";
        }
        else if (alertcity.equals("Kopar"))
        {	lat = "19.211113";
            lon = "73.077361";
        }
        else if (alertcity.equals("Kopar Khairane"))
        {	lat = "19.103146";
            lon = "73.011287";
        }
        else if (alertcity.equals("Kurla"))
        {	lat = "19.065578";
            lon = "72.879608";
        }
        else if (alertcity.equals("Lower Parel"))
        {	lat = "18.995364";
            lon = "72.829915";
        }
        else if (alertcity.equals("Lowjee"))
        {	lat = "18.808598";
            lon = "73.335400";
        }
        else if (alertcity.equals("Mahalaxmi"))
        {	lat = "18.982213";
            lon = "72.823972";
        }
        else if (alertcity.equals("Mahim"))
        {	lat = "19.040639";
            lon = "72.846852";
        }
        else if (alertcity.equals("Malad"))
        {	lat = "19.187222";
            lon = "72.848911";
        }
        else if (alertcity.equals("Mankhurd"))
        {	lat = "19.048312";
            lon = "72.931923";
        }
        else if (alertcity.equals("Mansarovar"))
        {	lat = "19.016605";
            lon = "73.080534";
        }
        else if (alertcity.equals("Marine Lines"))
        {	lat = "18.945795";
            lon = "72.823704";
        }
        else if (alertcity.equals("Masjid"))
        {	lat = "18.951395";
            lon = "72.838248";
        }
        else if (alertcity.equals("Matunga"))
        {	lat = "19.027637";
            lon = "72.850260";
        }
        else if (alertcity.equals("Matunga Road"))
        {	lat = "19.028157";
            lon = "72.846756";
        }
        else if (alertcity.equals("Mumbai Central"))
        {	lat = "18.971200";
            lon = "72.819467";
        }
        else if (alertcity.equals("Mumbai CST"))
        {	lat = "18.941917";
            lon = "72.835840";
        }
        else if (alertcity.equals("Mira Road"))
        {	lat = "19.280700";
            lon = "72.856026";
        }
        else if (alertcity.equals("Mulund"))
        {	lat = "19.171890";
            lon = "72.956476";
        }
        else if (alertcity.equals("Mumbra"))
        {	lat = "19.190189";
            lon = "73.023083";
        }
        else if (alertcity.equals("Nahur"))
        {	lat = "19.154670";
            lon = "72.946592";
        }
        else if (alertcity.equals("Naigaon"))
        {	lat = "19.351699";
            lon = "72.846181";
        }
        else if (alertcity.equals("Nalasopara"))
        {	lat = "19.417584";
            lon = "72.818874";
        }
        else if (alertcity.equals("Neral"))
        {	lat = "19.025925";
            lon = "73.318650";
        }
        else if (alertcity.equals("Nerul"))
        {	lat = "19.033602";
            lon = "73.018181";
        }
        else if (alertcity.equals("Navade Road"))
        {	lat = "19.052873";
            lon = "73.103487";
        }
        else if (alertcity.equals("Nilaje"))
        {	lat = "19.155274";
            lon = "73.080084";
        }
        else if (alertcity.equals("Oshiwara"))
        {	lat = "19.151634";
            lon = "72.849960";
        }
        else if (alertcity.equals("Palghar"))
        {	lat = "19.697797";
            lon = "72.772282";
        }
        else if (alertcity.equals("Palasdari"))
        {	lat = "18.884270";
            lon = "73.320832";
        }
        else if (alertcity.equals("Panvel"))
        {	lat = "18.990546";
            lon = "73.121467";
        }
        else if (alertcity.equals("Parel"))
        {	lat = "19.009267";
            lon = "72.837528";
        }
        else if (alertcity.equals("Rabale"))
        {	lat = "19.137789";
            lon = "73.003135";
        }
        else if (alertcity.equals("Reay road"))
        {	lat = "18.977268";
            lon = "72.844164";
        }
        else if (alertcity.equals("Roha"))
        {	lat = "18.447084";
            lon = "73.123588";
        }
        else if (alertcity.equals("Sandhurst Road"))
        {	lat = "18.961672";
            lon = "72.840081";
        }
        else if (alertcity.equals("Sanpada"))
        {	lat = "19.065913";
            lon = "73.009253";
        }
        else if (alertcity.equals("Santa Cruz"))
        {	lat = "19.082360";
            lon = "72.841725";
        }
        else if (alertcity.equals("Seawoods–Darave"))
        {	lat = "19.021721";
            lon = "73.019035";
        }
        else if (alertcity.equals("Sewri"))
        {	lat = "18.998956";
            lon = "72.854602";
        }
        else if (alertcity.equals("Saphale"))
        {	lat = "19.576739";
            lon = "72.822041";
        }
        else if (alertcity.equals("Sion"))
        {	lat = "19.046481";
            lon = "72.863193";
        }
        else if (alertcity.equals("Shahad"))
        {	lat = "19.244335";
            lon = "73.158273";
        }
        else if (alertcity.equals("Shelu"))
        {	lat = "19.062990";
            lon = "73.317825";
        }
        else if (alertcity.equals("Thane"))
        {	lat = "19.186035";
            lon = "72.975963";
        }
        else if (alertcity.equals("Thakurli"))
        {	lat = "19.226109";
            lon = "73.097910";
        }
        else if (alertcity.equals("Thansit"))
        {	lat = "19.550261";
            lon = "73.352070";
        }
        else if (alertcity.equals("Titwala"))
        {	lat = "19.296252";
            lon = "73.203085";
        }
        else if (alertcity.equals("Tilaknagar"))
        {	lat = "19.065859";
            lon = "72.889821";
        }
        else if (alertcity.equals("Turbhe"))
        {	lat = "19.076203";
            lon = "73.017893";
        }
        else if (alertcity.equals("Ulhasnagar"))
        {	lat = "19.218085";
            lon = "73.163140";
        }
        else if (alertcity.equals("Umbermali"))
        {	lat = "19.628311";
            lon = "73.422926";
        }
        else if (alertcity.equals("Umroli"))
        {	lat = "19.755619";
            lon = "72.760530";
        }
        else if (alertcity.equals("Uran"))
        {	lat = "18.878579";
            lon = "72.949361";
        }
        else if (alertcity.equals("Vadala Road"))
        {	lat = "19.016190";
            lon = "72.858980";
        }
        else if (alertcity.equals("Vaitarna"))
        {	lat = "19.518155";
            lon = "72.850021";
        }
        else if (alertcity.equals("Vangaon"))
        {	lat = "19.882957";
            lon = "72.763261";
        }
        else if (alertcity.equals("Vangani"))
        {	lat = "19.094328";
            lon = "73.300824";
        }
        else if (alertcity.equals("Vasai Road"))
        {	lat = "19.381072";
            lon = "72.832736";
        }
        else if (alertcity.equals("Vashi"))
        {	lat = "19.063067";
            lon = "72.998880";
        }
        else if (alertcity.equals("Vasind"))
        {	lat = "19.406680";
            lon = "73.267394";
        }
        else if (alertcity.equals("Vidyavihar"))
        {	lat = "19.079688";
            lon = "72.897489";
        }
        else if (alertcity.equals("Vikhroli"))
        {	lat = "19.111835";
            lon = "72.928236";
        }
        else if (alertcity.equals("Vile Parle"))
        {	lat = "19.099366";
            lon = "72.843901";
        }
        else if (alertcity.equals("Virar"))
        {	lat = "19.454878";
            lon = "72.811948";
        }
        else if (alertcity.equals("Vithalwadi"))
        {	lat = "19.228641";
            lon = "73.148783";
        }
        else if (alertcity.equals("Home"))
        {	lat = "19.2920018";
            lon = "73.2025144";
        }
        else
        {
            //Toast.makeText(getApplicationContext(),"Not found", Toast.LENGTH_SHORT).show();
        }
        et1 = (EditText)findViewById(R.id.et1);
        reminder = (et1.getText()).toString();
        LatDouble = Double.parseDouble(lat);
        LonDouble= Double.parseDouble(lon);
        CheckEnableLoc();
    }

    private void CheckEnableLoc() {
        String GEOFENCE_ID = "myGeofenceID";
        geoServiceIntent = new Intent(this, GeoFenceService.class);
        geoServiceIntent.putExtra("Latitude",LatDouble);
        geoServiceIntent.putExtra("Longitude",LonDouble);
        geoServiceIntent.putExtra("City", alertcity);
        geoServiceIntent.putExtra("Reminder", reminder);
        geoServiceIntent.putExtra("Id",GEOFENCE_ID);
        startService(geoServiceIntent);
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED)
        {
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",0);
        Editor editor = pref.edit();
        editor.putString("lat1", lat);
        editor.putString("lon1", lon);
        editor.putString("City", alertcity);
        editor.putString("Current alert", alertcity);
        editor.putString("Reminder", reminder);
        editor.putString("isrunning", "yes"); // Storing string
        editor.commit();
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfLoc = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean statusOfgps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (statusOfLoc||statusOfgps)
        {
            Toast.makeText(getApplicationContext(), "Alarm set for " + alertcity, Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetAlarmActivity.this);
            alertDialogBuilder.setTitle("Location service disabled!");
            alertDialogBuilder.setMessage("");
            alertDialogBuilder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);

                }
            });
            alertDialogBuilder.setNegativeButton("Cancel alarm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    Intent i2 = new Intent(getBaseContext(), GeoFenceService.class);
                    stopService(i2);
                    Intent i = new Intent(getBaseContext(), GeoFenceResetService.class);
                    stopService(i);
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
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
}
