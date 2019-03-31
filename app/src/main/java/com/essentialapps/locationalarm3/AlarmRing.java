package com.essentialapps.locationalarm3;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

public class AlarmRing extends Service{
    static Ringtone r;
    Vibrator v;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = RingtoneManager.getRingtone(getBaseContext(), notification);
        v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 1000};
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        r.play();
        long[] pattern = {0, 1000, 1000};
        v.vibrate(pattern, 0);
        return START_NOT_STICKY;

    }
    @Override
    public void onDestroy()
    {
        r.stop();
        v.cancel();
    }
}

