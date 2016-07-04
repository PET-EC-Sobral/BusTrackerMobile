package ufc.pet.bustracker;


import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import ufc.pet.bustracker.tools.ConnectionManager;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Route;

public class MapActivity extends AppCompatActivity implements
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

    // Gerenciador de conectividade
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Localiza elementos da interface
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mInfoTitle = (TextView) findViewById(R.id.info_title);
        mInfoDescription = (TextView) findViewById(R.id.info_description);
        mUpdateButton = (Button) findViewById(R.id.update_button);

        connectionManager = new ConnectionManager(this, getResources().getString(R.string.host_prefix));

        // Configura elementos da interface
        setSupportActionBar(mToolbar);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawRoutesOnMap();
            }
        });
        drawRoutesOnMap();

        // Atribui mapa ao elemento fragment na interface
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        LatLng routeStart = p.getPoints().get(0);
        LatLng routeEnd = p.getPoints().get(p.getPoints().size()-1);
        LatLng mean = new LatLng( (routeStart.latitude + routeEnd.latitude)/2,
                (routeStart.longitude + routeEnd.longitude) / 2);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mean, 15.5f));
    }

    /**
     * Desenha as rotas e armazena os polylines associados
     */
    public void drawRoutesOnMap(){
        connectionManager.getRoutesFromServer();
        routes = connectionManager.getRoutes();
        for(Route r : routes){
            if(!r.isActiveOnMap()) {
                Polyline p = mMap.addPolyline(
                        new PolylineOptions()
                                .addAll(r.getPoints())
                                .clickable(true)
                                .color(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                );
                r.setAssociatedPolyline(p);
            }
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
