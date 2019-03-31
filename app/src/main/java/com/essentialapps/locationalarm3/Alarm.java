package com.essentialapps.locationalarm3;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.essentialapps.locationalarm3.R;

public class Alarm extends Activity {
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    String LOG = "com.essentialapps.locationalarm3";
    private static final String TAG = "AlarmAct";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "incall");
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        } else {
        }
        Log.e(TAG, "Alarm triggered!");
        final Animation animscale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        final Animation animscale2 = AnimationUtils.loadAnimation(this, R.anim.scale2);
        final Animation animalpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
        final Animation animalpha2 = AnimationUtils.loadAnimation(this, R.anim.alpha2);
        Button b1 = (Button) findViewById(R.id.rbtn1);
        Button b2 = (Button) findViewById(R.id.rbtn2);
        Button b3 = (Button) findViewById(R.id.rbtn3);
        Button b4 = (Button) findViewById(R.id.rbtn4);
        this.getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD);
        AnimationSet sets = new AnimationSet(false);
        sets.addAnimation(animscale);
        sets.addAnimation(animalpha);
        b1.startAnimation(sets);
        AnimationSet sets1 = new AnimationSet(false);
        sets1.addAnimation(animscale2);
        sets1.addAnimation(animalpha2);
        b3.startAnimation(sets1);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String reminder = pref.getString("Reminder", "");
        TextView reminderview = (TextView) findViewById(R.id.reminderview);
        reminderview.setText(reminder);
        String title,stitle;
        reminder = pref.getString("Reminder", "");
        String s2 = pref.getString("City", "none");

        if (reminder.isEmpty())
        {
            title = "Reached! ";
            stitle = "At " +s2;
        }
        else {
            title = "Reached "+s2;
            stitle = "Remember to " + reminder;
        }
        Integer notif_active = 1001;
        createNotification(title,stitle,notif_active);
    }
    @SuppressLint("NewApi")
    private Notification createNotification(String title, String subtitle, Integer id) {
        Notification notification = new Notification();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setAutoCancel(true);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, Alarm.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        mBuilder.setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(Launcher.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(id, mBuilder.build());
        return notification;
    }
    public void exit()
    {
        Intent i = new Intent(getBaseContext(), AlarmRing.class);
        stopService(i);
        Intent i2 = new Intent(getBaseContext(), GeoFenceService.class);
        stopService(i2);
        Intent i4 = new Intent(getBaseContext(), GeoFenceResetService.class);
        stopService(i4);
        Toast.makeText(getApplicationContext(), "Have a nice day!", Toast.LENGTH_LONG).show();
        getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exit();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }
}
