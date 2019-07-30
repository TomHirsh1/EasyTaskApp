package tomhirsh2.gmail.com.easytaskapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

import tomhirsh2.gmail.com.easytaskapp.TaskHome;

// class to update location from background
public class mLocationService extends BroadcastReceiver {
    public  static final String ACTION_PROCESS_UPDATE = "tomhirsh2.gmail.com.easytaskapp.services.UPDATE_LOCATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            final String action = intent.getAction();
            if(ACTION_PROCESS_UPDATE.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if(result != null) {
                    Location location = result.getLastLocation();
                    String location_string = new StringBuilder(""+location.getLatitude())
                            .append("/")
                            .append(location.getLongitude())
                            .toString();
                    try {
                        Toast.makeText(context, "This actually works", Toast.LENGTH_LONG).show();
                        // here we need to send location_string to TaskHome
                        // and use it there. this is our current location.
                        //TaskHome.getInstance();
                    } catch(Exception e) {
                        Toast.makeText(context, "location_string", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
}
