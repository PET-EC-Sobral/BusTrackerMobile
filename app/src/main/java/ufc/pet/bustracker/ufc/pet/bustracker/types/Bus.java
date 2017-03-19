package ufc.pet.bustracker.ufc.pet.bustracker.types;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.threeten.bp.LocalDateTime;

import java.util.Date;

public class Bus {
    private int id;
    private LatLng coordinates;
    private double velocity;
    private LocalDateTime lastUpdate;
    private Marker associatedMarker;

    public Bus(){

        coordinates = new LatLng(0,0);
        velocity = 0;
        associatedMarker = null;
    }

    public void updateLocation(double latitude, double longitude, double velocity, LocalDateTime updateTime){
        this.coordinates = new LatLng(latitude, longitude);
        this.velocity = velocity;
        this.lastUpdate = updateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Marker getAssociatedMarker() {
        return associatedMarker;
    }

    public void setAssociatedMarker(Marker associatedMarker) {
        this.associatedMarker = associatedMarker;
    }

    public boolean isActiveOnMap() {
        return associatedMarker != null;
    }

}
