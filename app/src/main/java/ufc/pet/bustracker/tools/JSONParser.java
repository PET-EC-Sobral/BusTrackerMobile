package ufc.pet.bustracker.tools;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

import ufc.pet.bustracker.MapActivity;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Bus;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Route;

/**
 * Ferramenta para interpretar o JSON das rotas e dos ônibus
 */
public class JSONParser {
    public Route parseRoute(JSONObject ob) throws JSONException {
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
        } catch (Throwable t){
            throw t;
        }
        return r;
    }

    public Bus parseBus(JSONObject ob) throws JSONException {

        Bus b = new Bus();
        try {
            b.setId(ob.getInt("id_bus"));
            b.setVelocity(ob.getDouble("velocity"));
            JSONArray lastLocalizations = ob.getJSONArray("lastLocalizations");
            JSONObject locationInfo = lastLocalizations.getJSONObject(0);
            Double lat = locationInfo.getDouble("latitude");
            Double lng = locationInfo.getDouble("longitude");
            b.setCoordinates(new LatLng(lat, lng));

            String ultima_atualizacao = locationInfo.getString("date");
            b.setLastUpdate(LocalDateTime.parse(ultima_atualizacao, DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss")));
        } catch (Throwable t) {
            throw t;
        }
        return b;
    }
}
