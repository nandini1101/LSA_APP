package com.org.lsa.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import com.org.lsa.custom.Utility;

public class GPSReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("android.location.PROVIDERS_CHANGED".equals(intent.getAction())) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!(locationManager != null && locationManager.isProviderEnabled("gps"))) {
                Utility.checkGPSEnabled(context);
            }
        }
    }
}
