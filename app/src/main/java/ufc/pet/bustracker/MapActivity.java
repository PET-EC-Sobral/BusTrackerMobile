package ufc.pet.bustracker;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ufc.pet.bustracker.tools.JSONParser;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Bus;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Route;

public class MapActivity extends AppCompatActivity implements
        View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener
{

    // Tag para os logs
    public static final String TAG = MapActivity.class.getName();

    // Elementos da interface
    private GoogleMap mMap;
    private Toolbar mToolbar;
    private TextView mInfoTitle;
    private TextView mInfoDescription;
    private Button mUpdateButton;
    private ArrayList<Route> routes;
    private ArrayList<Bus> buses;
    private ProgressDialog progressDialog;

    // Gerenciador de conectividade
    private RequestQueue requestQueue;
    private String serverPrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Localiza elementos da interface
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mInfoTitle = (TextView) findViewById(R.id.info_title);
        mInfoDescription = (TextView) findViewById(R.id.info_description);
        mUpdateButton = (Button) findViewById(R.id.update_button);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        routes = new ArrayList<>(0);
        buses = new ArrayList<>(0);
        serverPrefix = getResources().getString(R.string.host_prefix);

        // Configura elementos da interface
        setSupportActionBar(mToolbar);
        mUpdateButton.setOnClickListener(this);

        // Atribui mapa ao elemento fragment na interface
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_button:
                progressDialog = ProgressDialog.show(MapActivity.this, "Aguarde...",
                        "Carregando informações");
                getRoutesFromServer();
                break;
        }
    }

    /**
     * Ações ao clicar em um polyline
     */
    public void onPolylineClick(Polyline p){
        int selected = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        int unselected = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        for(Route r : routes){
            if(r.isActiveOnMap()){
                Polyline routePoly = r.getAssociatedPolyline();
                if(routePoly.hashCode() == p.hashCode()) {
                    mInfoTitle.setText(r.getName());
                    mInfoDescription.setText(r.getDescription());
                } else {
                    routePoly.setColor(unselected);
                }
            }
        }
        p.setColor(selected);
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for(LatLng l : p.getPoints()){
            b.include(l);
        }
        LatLngBounds bounds = b.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));
        getBusesOnRoute(87);
    }

    public void getRoutesFromServer() {
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
                            drawRoutesOnMap();
                            progressDialog.dismiss();
                        } catch (Exception e) {
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

    public void getBusesOnRoute(int id) {
        String url = serverPrefix + "/routes/" + id + "/buses?localizations=1";
        JsonArrayRequest jreq = new JsonArrayRequest(JsonArrayRequest.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONParser parser = new JSONParser();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject ob = response.getJSONObject(i);
                                Bus b = parser.parseBus(ob);
                                buses.add(b);
                            }
                            markBusesOnMap();
                        } catch (Exception e) {
                            Log.e(MapActivity.TAG, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(MapActivity.TAG, error.getMessage());
                    }
                });
        requestQueue.add(jreq);
    }

    /**
     * Desenha as rotas e armazena os polylines associados
     */
    public void drawRoutesOnMap(){
        for(Route r : routes){
            if (r.isActiveOnMap())
                return;
            Polyline p = mMap.addPolyline(
                    new PolylineOptions()
                            .addAll(r.getPoints())
                            .clickable(true)
                            .color(ContextCompat.getColor(
                                    getApplicationContext(),
                                    R.color.colorPrimary))
            );
            r.setAssociatedPolyline(p);
        }
    }

    public void markBusesOnMap() {
        for (Bus b : buses) {
            if (b.isActiveOnMap())
                return;
            Log.e(MapActivity.TAG, "Chegou aqui");
            Marker m = mMap.addMarker(
                    new MarkerOptions()
                            .position(b.getCoordinates())
                            .title("Ônibus " + b.getId())
            );
            b.setAssociatedMarker(m);
        }
    }

    /**
     * Fornece um manipulador para o objeto do mapa
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnPolylineClickListener(this);
        // Posiciona o mapa em Sobral
        LatLng sobral = new LatLng(-3.6906438,-40.3503957);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sobral, 15));
    }

    /**
     * Cria um menu de opções na barra superior
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
