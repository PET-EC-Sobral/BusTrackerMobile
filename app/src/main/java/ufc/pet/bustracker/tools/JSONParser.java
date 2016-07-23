package ufc.pet.bustracker.tools;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import ufc.pet.bustracker.MapActivity;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Bus;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Route;

public class JSONParser {
    public Route parseRoute(JSONObject ob){
        Route r = new Route();
        try{
            r.setId_routes(ob.getInt("id_routes"));
            r.setName(ob.getString("name"));
            r.setDescription(ob.getString("description"));
            JSONArray id_buses = ob.getJSONArray("id_buses");
            String polylineCode = ob.getJSONObject("googleRoute").getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
            r.setPoints(PolyUtil.decode(polylineCode));
            for(int i = 0; i < id_buses.length(); i++){
                int id = id_buses.getInt(i);
                r.getId_buses().add(id);
            }

        } catch (Exception e){
            Log.e(MapActivity.TAG, e.getMessage());
        }
        return r;
    }

    public Bus parseBus(JSONObject ob) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss");
        Bus b = new Bus();
        try {
            b.setId(ob.getInt("id_bus"));
            b.setVelocity(ob.getDouble("velocity"));
            JSONArray lastLocalizations = ob.getJSONArray("lastLocalizations");
            JSONObject locationInfo = lastLocalizations.getJSONObject(0);
            Double lat = locationInfo.getDouble("latitude");
            Double lng = locationInfo.getDouble("longitude");
            Date d;
            d = df.parse(locationInfo.getString("date"));
            b.setCoordinates(new LatLng(lat, lng));
            b.setLastUpdate(d);
        } catch (Exception e) {
            Log.e(MapActivity.TAG, e.getMessage());
        }
        return b;
    }
}
