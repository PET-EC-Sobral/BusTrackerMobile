package ufc.pet.bustracker;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by abner on 22/06/2016.
 */
public class JSONParser {
    private JSONArray bus1 = null;
    private JSONArray bus2 = null;

    private String json;

    public JSONParser(String json){
        this.json = json;
    }

    public LatLng[] parseJSON(){
        JSONObject jsonObject = null;
        LatLng[] result = new LatLng[2];
        try {
            jsonObject = new JSONObject(json);
            bus1 = jsonObject.getJSONArray("bus1");
            bus2 = jsonObject.getJSONArray("bus2");
            LatLng coords1 = new LatLng(bus1.getDouble(0),bus1.getDouble(1));
            LatLng coords2 = new LatLng(bus2.getDouble(0),bus2.getDouble(1));
            result[0] = coords1;
            result[1] = coords2;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
