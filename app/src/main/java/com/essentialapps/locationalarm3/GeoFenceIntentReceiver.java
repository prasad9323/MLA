package com.essentialapps.locationalarm3;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

public class GeoFenceIntentReceiver extends IntentService {

    protected static final String LOG_TAG = GeoFenceIntentReceiver.class.getName();

    public GeoFenceIntentReceiver() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError())
        {
            Log.e(LOG_TAG,"geofencingEvent hasError");
        }
        else{
            int transition = geofencingEvent.getGeofenceTransition();
            List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
            Geofence geofence = geofenceList.get(0);
            String requestId = geofence.getRequestId();
            if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.d(LOG_TAG,String.format("GEOFENCE_TRANSITION_ENTER on %s",requestId));
                Intent i = new Intent(this, Alarm.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                Intent i2 = new Intent(getApplicationContext(), AlarmRing.class);
                getApplicationContext().startService(i2);
                //showToast("Entering");
            }
            else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                Log.d(LOG_TAG,String.format("GEOFENCE_TRANSITION_EXIT  on %s",requestId));
            }
        }
    }

    public void showToast (String string)
    {
        Toast.makeText(getApplicationContext(), string , Toast.LENGTH_LONG).show();
    }
}

