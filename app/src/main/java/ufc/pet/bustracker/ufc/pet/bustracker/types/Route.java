package ufc.pet.bustracker.ufc.pet.bustracker.types;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private Integer id_routes;
    private String name;
    private String description;
    private ArrayList<Integer> id_buses;
    private List<LatLng> points;
    private Polyline associatedPolyline;

    public Route(){
        id_buses = new ArrayList<Integer>();
        points = new ArrayList<LatLng>();
    }

    public Integer getId_routes() {
        return id_routes;
    }

    public void setId_routes(Integer id_routes) {
        this.id_routes = id_routes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Integer> getId_buses() {
        return id_buses;
    }

    public void setId_buses(ArrayList<Integer> id_buses) {
        this.id_buses = id_buses;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    public Polyline getAssociatedPolyline(){
        return associatedPolyline;
    }
    public void setAssociatedPolyline(Polyline p){
        associatedPolyline = p;
    }

    // Verifica se existe um polyline associado
    public boolean isActiveOnMap() {
        return associatedPolyline != null;
    }

}
