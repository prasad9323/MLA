package com.essentialapps.locationalarm3;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Launcher extends AppCompatActivity {

    String s8,recipient,subject;
    CheckBox gpsbox,intbox ;
    Intent geoServiceIntent;
    boolean mServiceBound = false;
    GeoFenceService geoFenceService;
    private NotificationReceiver nReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        AdView mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAdView.loadAd(adRequest);
        geoServiceIntent = new Intent(this, GeoFenceService.class);
        bindService(geoServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Intent i = new Intent(getApplicationContext(),GeoFenceResetService.class);
        startService(i);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",0);
        s8 = pref.getString("Current alert", "None"); // getting String
        if (!s8.equals("None"))
        {
            String GEOFENCE_ID = "myGeofenceID";
            String lat = pref.getString("lat1", null);
            String lon = pref.getString("lon1", null);
            String add = pref.getString("City", null);
            String reminder = pref.getString("Reminder","");
            geoServiceIntent = new Intent(this, GeoFenceService.class);
            geoServiceIntent.putExtra("Latitude",lat);
            geoServiceIntent.putExtra("Longitude",lon);
            geoServiceIntent.putExtra("City", add);
            geoServiceIntent.putExtra("Reminder", reminder);
            geoServiceIntent.putExtra("Id",GEOFENCE_ID);
            startService(geoServiceIntent);
        }
        gpsbox = (CheckBox)findViewById(R.id.gpsbox);
        intbox = (CheckBox)findViewById(R.id.intbox);
        CheckEnableLoc();
        CheckInternet();
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
        registerReceiver(nReceiver,filter);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Launcher.this, SetAlarmActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.animation3, R.anim.animation4);
            }
        });
        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this,PERMISSIONS,1);
        Button b1 = (Button)findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Launcher.this, SetAlarmActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.animation3, R.anim.animation4);
            }
        });
        Button b2 = (Button)findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlert(v);
            }
        });
        Button b3 = (Button)findViewById(R.id.loc);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        });
        Button b4 = (Button)findViewById(R.id.data);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(i);
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 100);
    }

    private void openAlert(View view) {
        if(s8.equals("None"))
        {
            Toast.makeText(getApplicationContext(),"Alert not set", Toast.LENGTH_LONG).show();
        }
        else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Launcher.this);
            alertDialogBuilder.setTitle("Cancel the alarm?");
            alertDialogBuilder.setMessage("");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent i2 = new Intent(getBaseContext(), GeoFenceService.class);
                    stopService(i2);
                    Intent i = new Intent(getBaseContext(), GeoFenceResetService.class);
                    stopService(i);
                    Toast.makeText(getApplicationContext(),"Alarm cancelled", Toast.LENGTH_SHORT).show();
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("Current alert", "None");
                    editor.putString("isrunning", "no"); // Storing string
                    editor.commit();
                    update();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            update();
                        }
                    }, 100);
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private void update() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",0);
        s8 = pref.getString("Current alert", "None"); // getting String
        TextView t1 = (TextView)findViewById(R.id.textView2);
        t1.setText(s8);
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent i = new Intent(getApplicationContext(),About.class);
            startActivity(i);
            overridePendingTransition(R.anim.animation3, R.anim.animation4);
            return true;
        }
        if (id == R.id.action_faq) {
            Intent i = new Intent(getApplicationContext(),FAQ.class);
            startActivity(i);
            overridePendingTransition(R.anim.animation3, R.anim.animation4);
            return true;
        }
        if (id == R.id.action_help) {
            Intent i = new Intent(getApplicationContext(),IntroAct.class);
            startActivity(i);
            overridePendingTransition(R.anim.animation, R.anim.animation2);
            return true;
        }
        if (id == R.id.action_contact) {
            sendEmail();
            return true;
        }
        if (id == R.id.action_exit) {
            getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void sendEmail() {
        recipient = this.getString(R.string.dev_email_id);
        subject = "About the app...";
        String[] recipients = {recipient.toString()};
        Intent email = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
        email.setType("message/rfc822");
        email.putExtra(Intent.EXTRA_SUBJECT, subject.toString());
        email.putExtra(Intent.EXTRA_EMAIL, recipients);
        try {
            startActivity(Intent.createChooser(email, "Choose an email client from..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email client installed.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void CheckEnableLoc() {
        CountDownTimer x = new CountDownTimer(3000, 2000) {
            public void onTick(long millisUntilFinished) {
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
                boolean statusOfLoc = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                boolean statusOfgps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (statusOfLoc||statusOfgps)
                {
                    gpsbox.setChecked(true);
                }
                else
                {
                    gpsbox.setChecked(false);
                }
            }
            public void onFinish() {
                start();
            }
        };
        x.start();
    }

    private void CheckInternet() {
        CountDownTimer x = new CountDownTimer(3000, 2000) {
            public void onTick(long millisUntilFinished) {
                checkInternetConenction();
                if (checkInternetConenction())
                {
                    intbox.setChecked(true);
                }
                else
                {
                    intbox.setChecked(false);
                }
            }
            public void onFinish() {
                start();
            }
        };
        x.start();
    }

    private boolean checkInternetConenction() {
        ConnectivityManager connec =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED  ) {
            return false;
        }
        return false;
    }

    @Override
    public void finish() {
        super.finish();
    }
    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
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
