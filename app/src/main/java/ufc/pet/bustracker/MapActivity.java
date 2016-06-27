package ufc.pet.bustracker;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener {

    // Elementos da interface
    private GoogleMap mMap;
    private Toolbar mToolbar;
    private Button mUpdateButton;
    private Marker[] busMarkers;

    // Parâmetros da conexão
    private static final String JSON_URL = "http://random_locations.netne.net/coords.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Localiza elementos da interface
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mUpdateButton = (Button) findViewById(R.id.update_button);

        // Configura elementos da interface
        mUpdateButton.setOnClickListener(this);
        setSupportActionBar(mToolbar);

        // Atribui mapa ao elemento fragment na interface
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Fornece um manipulador para o objeto do mapa
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Posiciona o mapa em Sobral
        LatLng sobral = new LatLng(-3.6906438,-40.3503957);
        busMarkers = new Marker[2];
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sobral, 14));

        // Adiciona marcadores para os ônibus
        busMarkers[0] = mMap.addMarker(new MarkerOptions().position(sobral));
        busMarkers[1] = mMap.addMarker(new MarkerOptions().position(sobral));

        sendRequest();
    }

    /**
     * Cria um menu de opções na barra superior
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.manu, menu);
        return true;
    }

    /**
     * Controla açao de clique para o botão atualizar
     */
    @Override
    public void onClick(View v){
        sendRequest();
    }

    /**
     * Envia requisição ao servidor
     */
    private void sendRequest(){
        StringRequest stringRequest = new StringRequest(JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONParser parser = new JSONParser(response);
                        LatLng[] locations = parser.parseJSON();
                        updateMap(locations);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /**
     * Marca locais obtidos no mapa
     */
    private void updateMap(LatLng[] locations){
        for (int i=0;i<locations.length;i++){
            busMarkers[i].setPosition(locations[i]);
        }
    }
}
