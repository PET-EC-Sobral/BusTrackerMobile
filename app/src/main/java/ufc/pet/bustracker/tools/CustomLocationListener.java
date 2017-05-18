package ufc.pet.bustracker.tools;

/**
 * Created by Isaben on 17/05/2017.
 */

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class CustomLocationListener implements LocationListener{

    private Location user_location;

    public CustomLocationListener(){
        user_location = null;
    }

    public void onLocationChanged(Location location){
        user_location = location;
    }

    public void onProviderEnabled(String provider){
        Log.d("LOCATION LISTENER", "ATIVADO!");
    }

    public void onProviderDisabled(String provider){
        Log.d("LOCATION LISTENER", "DESATIVADO!");
    }

    public Location getUser_location(){
        return user_location;
    }

    public double distance(LatLng bus){
        Location bus_location = new Location("");
        bus_location.setLatitude(bus.latitude);
        bus_location.setLongitude(bus.longitude);

        return bus_location.distanceTo(user_location);
    }

    public void onStatusChanged(String provider, int status, Bundle extras){
        Log.d("STATUS CHANGED", "Do nothing");
    }
}
