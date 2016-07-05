package ufc.pet.bustracker.ufc.pet.bustracker.types;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

public class Bus {
    private int id;
    private LatLng coordinates;
    private double velocity;
    private ArrayList<Date> lastUpdates;

    public Bus(){
        lastUpdates = new ArrayList<>(0);
        coordinates = new LatLng(0,0);
        velocity = 0;
    }

    public void updateLocation(double latitude, double longitude, double velocity, Date updateTime){
        this.coordinates = new LatLng(latitude, longitude);
        this.velocity = velocity;
        this.lastUpdates.add(updateTime);
    }

    public Date getLastUpdate(){
        return lastUpdates.get(0);
    }
}
