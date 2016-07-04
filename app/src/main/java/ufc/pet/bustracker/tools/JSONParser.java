package ufc.pet.bustracker.tools;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import ufc.pet.bustracker.MapActivity;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Route;

public class JSONParser {
    public Route parseRoute(JSONObject ob){
        Route r = new Route();
        try{
            r.setId_routes(ob.getInt("id_routes"));
            r.setName(ob.getString("name"));
            r.setDescription(ob.getString("description"));
            JSONArray id_buses = ob.getJSONArray("id_buses");
            JSONArray points = ob.getJSONArray("points");
            for(int i = 0; i < id_buses.length(); i++){
                int id = id_buses.getInt(i);
                r.getId_buses().add(id);
            }
            for(int i = 0; i < points.length(); i++){
                JSONObject point = points.getJSONObject(i);
                Double lat = point.getDouble("latitude");
                Double lng = point.getDouble("longitude");
                LatLng p = new LatLng(lat, lng);
                r.getPoints().add(p);
            }
        } catch (Exception e){
            Log.e(MapActivity.TAG, e.getMessage());
        }
        return r;
    }
}
