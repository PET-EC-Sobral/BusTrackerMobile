package ufc.pet.bustracker.tools;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

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

    public String getServerPrefix() {
        return this.serverPrefix;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }
}

