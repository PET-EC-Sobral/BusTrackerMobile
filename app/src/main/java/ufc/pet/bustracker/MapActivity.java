package ufc.pet.bustracker;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
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

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import ufc.pet.bustracker.tools.CustomJsonArrayRequest;
import ufc.pet.bustracker.tools.CustomJsonObjectRequest;
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
    private ArrayList<Marker> busOnScreen;
    private ProgressDialog progressDialog;

    // Gerenciador de conectividade
    private RequestQueue requestQueue;
    private String serverPrefix;

    // Handler para lidar com atualização/notificação automática
    private Handler handler = new Handler();

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
        busOnScreen = new ArrayList<>();
        serverPrefix = getResources().getString(R.string.host_prefix);

        // Configura elementos da interface
        setSupportActionBar(mToolbar);
        mUpdateButton.setOnClickListener(this);

        // Atribui mapa ao elemento fragment na interface
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        progressDialog = ProgressDialog.show(MapActivity.this, "Aguarde...",
                "Carregando informações");
        getRoutesFromServer();
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
        getBusesOnRoute(86);
        handler.postDelayed(updateBus, 0);
    }

    public void getRoutesFromServer() {
        String url = serverPrefix + "/routes/86"; // apenas uma rota por que só ela está atualizada com ônibus
        Log.e("teste", url);
        //token para teste
        String token = "Rs1cFdbn9yOY7V\\/SYWmVIbj3PYvVZo+H7WhLd9GOQ3lCwMhgPzot2WRm4hx25i+wrrmhkX5InH5ZlbaLJ2hPLiK9ThVwgSAWSY5T\\/v1hoztNESEWtTPX+2YcwfJ\\/p7kDftRatLTDcpFZ2Dn\\/7LhEwNUn35OafWyA+Cus9wRQ0n3I6xMoWSjGXj1IAgJi44BrKex\\/PMS7lWm0ZK261Wksx4Vj0\\/YRQJgzsGMluG8HNes=";
        JsonObjectRequest jreq = new CustomJsonObjectRequest(JsonObjectRequest.Method.GET, url, null,token ,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONParser parser = new JSONParser();
                        try {

                            Route r = parser.parseRoute(response);
                            routes.add(r);
                            progressDialog.dismiss();
                            drawRoutesOnMap();
                        } catch (Exception e){
                            Log.e(MapActivity.TAG, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("fuuudeu", error.toString());
                        progressDialog.dismiss();
                    }
                });
        requestQueue.add(jreq);
    }

    public void getBusesOnRoute(int id) {
        String url = serverPrefix + "/routes/" + id + "/buses?localizations=1";
        //token para teste
        String token = "Rs1cFdbn9yOY7V\\/SYWmVIbj3PYvVZo+H7WhLd9GOQ3lCwMhgPzot2WRm4hx25i+wrrmhkX5InH5ZlbaLJ2hPLiK9ThVwgSAWSY5T\\/v1hoztNESEWtTPX+2YcwfJ\\/p7kDftRatLTDcpFZ2Dn\\/7LhEwNUn35OafWyA+Cus9wRQ0n3I6xMoWSjGXj1IAgJi44BrKex\\/PMS7lWm0ZK261Wksx4Vj0\\/YRQJgzsGMluG8HNes=";
        JsonArrayRequest jreq = new CustomJsonArrayRequest(JsonArrayRequest.Method.GET, url, null, token,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONParser parser = new JSONParser();
                        try {
                            buses.clear();
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

    /**
     * Marca os ônibus no mapa de acordo com os que estão ativos, dado o vetor buses
     * que é preenchido no recebimento da informação do servidor na funçao getBusesOnRoute.
     */
    public void markBusesOnMap() {
        if(busOnScreen.size() != 0){
            for(int i = 0; i < busOnScreen.size(); i++){
                busOnScreen.get(i).remove();
            }
            busOnScreen.clear();

        }
        for (Bus b : buses) {
            Log.e(MapActivity.TAG, "Chegou aqui");
            Marker m = mMap.addMarker(
                    new MarkerOptions()
                            .position(b.getCoordinates())
                            .title("Ônibus " + b.getId())
            );
            busOnScreen.add(m);
            b.setAssociatedMarker(m);
        }


        /* Não funciona... por algum motivo? Acredito que os marcadores
        que ficam salvos nos objetos perdem a referência no mapa, aí quando vem um novo
        ele não remove, mesmo quando a função remove() é chamada
        for (Bus b : buses) {
            if (b.isActiveOnMap())
                b.getAssociatedMarker().remove();
            Log.e(MapActivity.TAG, "Chegou aqui");
            Marker m = mMap.addMarker(
                    new MarkerOptions()
                            .position(b.getCoordinates())
                            .title("Ônibus " + b.getId())
            );
            b.setAssociatedMarker(m);
        }*/
    }

    /**
     * Atualizar os ônibus automaticamente no mapa
     */
    Runnable updateBus = new Runnable(){
        @Override
        public void run(){
            for(Route r : routes){
                if(r.isActiveOnMap()) {
                    getBusesOnRoute(r.getId_routes());
                }
            }
            handler.postDelayed(updateBus, 3000);
        }
    };
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


    public void onClickAbout(MenuItem item){
        startActivity(new Intent(MapActivity.this, AboutActivity.class));
    }


    public void onClickSettings(MenuItem item){
        return;
    }


}
