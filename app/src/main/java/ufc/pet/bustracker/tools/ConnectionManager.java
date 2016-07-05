package ufc.pet.bustracker.tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ufc.pet.bustracker.MapActivity;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Route;

public class ConnectionManager{
    private RequestQueue requestQueue;
    private final String serverPrefix;
    private Context context;
    private ArrayList<Route> routes;

    public ConnectionManager(Context context, String serverPrefix){
        this.serverPrefix = serverPrefix;
        this.context = context;
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        routes = new ArrayList<>(0);
    }

    public void getRoutesFromServer(){
        String url = serverPrefix + "/routes/?points=true";
        JsonArrayRequest jreq = new JsonArrayRequest(JsonArrayRequest.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONParser parser = new JSONParser();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject ob = response.getJSONObject(i);
                                Route r = parser.parseRoute(ob);
                                routes.add(r);
                            }
                            Toast.makeText(context, "Rotas obtidas!", Toast.LENGTH_SHORT);
                        } catch (Exception e){
                            Log.e(MapActivity.TAG, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(jreq);
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }
}

