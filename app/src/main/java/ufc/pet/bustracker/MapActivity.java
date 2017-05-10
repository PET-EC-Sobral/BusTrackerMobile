/**
 *  PROJETO BUS TRACKER
 *  UNIVERSIDADE FEDERAL DO CEARÁ
 *  PROGRAMA DE EDUCAÇÃO TUTORIAL
 *  SOBRAL, CE - 2016
 *
 *  ATIVIDADE PRINCIPAL (MapActivity)
 *
 *  Este arquivo contém o código da tela principal do aplicativo. Nela é possível selecionar rotas,
 *  além de visualizar os ônibus e informações.
 */

package ufc.pet.bustracker;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONArray;
import org.json.JSONObject;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ufc.pet.bustracker.tools.CustomJsonArrayRequest;
import ufc.pet.bustracker.tools.CustomJsonObjectRequest;
import ufc.pet.bustracker.tools.JSONParser;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Bus;
import ufc.pet.bustracker.ufc.pet.bustracker.types.Route;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnMarkerClickListener
{
    // Preferências salvas
    SharedPreferences pref;

    // Tag para os logs
    public static final String TAG = MapActivity.class.getName();

    // Elementos da interface
    private GoogleMap mMap;
    private Toolbar mToolbar;
    private TextView mInfoTitle;
    private TextView mInfoDescription;
    private ProgressDialog progressDialog;
    private FloatingActionMenu fabMenu;
    private TextView busUpdateInfo;
    private ImageView imageInfo;

    // Controle de cliques
    private boolean lastClickWasABus = false;

    // Abstrações dos ônibus e rotas
    private ArrayList<Route> routes;
    private ArrayList<Bus> buses;
    private ArrayList<Marker> busOnScreen;
    private Route selectedRoute = null;

    // Gerenciador de conectividade
    private RequestQueue requestQueue;
    private String serverPrefix;
    private String token;

    // Handler para lidar com atualização/notificação automática
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Localização dos elementos da interface
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mInfoTitle = (TextView) findViewById(R.id.info_title);
        mInfoDescription = (TextView) findViewById(R.id.info_description);
        fabMenu = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        busUpdateInfo = (TextView) findViewById(R.id.info_update);
        imageInfo = (ImageView) findViewById(R.id.image_info);

        // Configurações de conectividade
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        serverPrefix = getResources().getString(R.string.host_prefix);
        token = pref.getString(getString(R.string.token), "null");

        // Abstrações que representam os ônibus e rotas
        routes = new ArrayList<>(0);
        buses = new ArrayList<>(0);
        busOnScreen = new ArrayList<>();

        // Configura os elementos da interface
        setSupportActionBar(mToolbar);
        fabMenu.setIconAnimated(false);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        progressDialog = ProgressDialog.show(MapActivity.this, "Aguarde...",
                "Carregando informações");

        // Carrega informações iniciais
        getRouteFromServer(86);

        //TODO: O sistema de requisição de rotas múltiplas precisa ser repensado para trabalhar em conjunto com o novo botão flutuante
        getBusesOnRoute(86);
        handler.postDelayed(updateBus, 0);
    }

    /**
     * Controla o que acontece após uma rota ser clicada
     * @param p Objeto polyline que representa a rota
     */
    public void onPolylineClick(Polyline p){
        imageInfo.setVisibility(View.GONE);
        if(selectedRoute != null){
            Polyline routePoly = selectedRoute.getAssociatedPolyline();
            if(routePoly.hashCode() == p.hashCode()) {
                setTitleAndDescription(selectedRoute.getName(), selectedRoute.getDescription());
            }
        }

        // Movimenta a câmera para visualizar a rota completamente
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for(LatLng l : p.getPoints()){
            b.include(l);
        }
        LatLngBounds bounds = b.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));
        lastClickWasABus = false;
    }


    /**
     * Carrega as rotas a partir do servidor
     * DEPRECATED
     *
    */
    /*
    public void getRoutesFromServer() {
        String url = serverPrefix + "/routes?points=true";

        JsonArrayRequest jreq = new CustomJsonArrayRequest(JsonArrayRequest.Method.GET, url, null, token,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONParser parser = new JSONParser();
                        try {
                            routes.clear();
                            for(int i = 0; i < response.length(); i++) {
                                JSONObject o = response.getJSONObject(i);
                                Route r = parser.parseRoute(o);
                                routes.add(r);
                                progressDialog.dismiss();
                            }
                            setupRouteButtons();
                        } catch (Exception e){
                            Log.e(MapActivity.TAG, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(MapActivity.TAG, error.toString());
                        progressDialog.dismiss();
                    }
                });
        requestQueue.add(jreq);
    }
    */
    /**
     * Carrega uma única rota em selecteRoute
     * @param id Id da rota a ser carregada
     */
    public void getRouteFromServer(int id) {
        String url = serverPrefix + "/routes/" + id;
        routes.clear();
        JsonObjectRequest jreq = new CustomJsonObjectRequest(JsonObjectRequest.Method.GET, url, null,token ,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONParser parser = new JSONParser();
                        try {
                            Route r = parser.parseRoute(response);
                            routes.add(r);
                            drawRouteOnMap(r);
                            selectedRoute = r;
                            setupRouteButtons();
                        } catch (Exception e){
                            Log.e(MapActivity.TAG, e.getMessage());
                            setTitleAndDescription(getString(R.string.erro_info_indisponivel_title),
                                                   getString(R.string.erro_info_indisponivel_msg));
                        }
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(MapActivity.TAG, error.toString());
                        setTitleAndDescription(getString(R.string.erro_indisponivel_title),
                                               getString(R.string.erro_indisponivel_msg));
                        progressDialog.dismiss();
                    }
                });
        //progressDialog.dismiss();
        requestQueue.add(jreq);
    }

    /**
     * Configura o texto do título e da descrição
     * @param title Título a ser exibido
     * @param description Descrição a ser exibida
     */
    private void setTitleAndDescription(String title, String description){
        mInfoTitle.setText(title);
        mInfoDescription.setText(description);
        busUpdateInfo.setVisibility(View.GONE);
    }

    /**
     * Configura o texto do título e da descrição, com adição do último update do ônibus, quando
     * necessário
     * @param title Título a ser exibido
     * @param description Descrição a ser exibida
     * @param lastUpdate último update do ônibus
     */
    private void setTitleAndDescription(String title, String description, LocalDateTime lastUpdate){
        mInfoTitle.setText(title);
        mInfoDescription.setText(description);
        LocalDateTime atual = LocalDateTime.now();

        Log.d("ONIBUS", "Ultima atualizacao as "+lastUpdate.toString()+".");
        Log.d("ONIBUS", "Hora atual "+atual.toString()+".");
        long tempo_total = Duration.between(lastUpdate, atual).toMillis()/1000;
        Log.d("ONIBUS", "Tempo total "+atual.toString()+".");
        long horas = tempo_total / 3600;
        long minutos = (tempo_total % 3600)/60;
        long segundos = ((tempo_total % 3600)%60);

        busUpdateInfo.setVisibility(View.VISIBLE);
        String ultimaAtualizacao = "Última atualização há ";
        if(horas > 0) {
            ultimaAtualizacao += String.valueOf(horas) + " horas, ";
        }
        if(minutos > 0){
            ultimaAtualizacao += String.valueOf(minutos)+ " minutos e ";
        }
        ultimaAtualizacao += String.valueOf(segundos)+" segundos.";
        busUpdateInfo.setText(ultimaAtualizacao);

    }

    /**
     * Cria no máximo 3 botões de navegação flutuantes para as rotas
     */
    private void setupRouteButtons(){
        fabMenu.setVisibility(View.INVISIBLE);
        fabMenu.removeAllMenuButtons();
        if(routes.isEmpty()){
            setTitleAndDescription(getString(R.string.title_noroutes),
                                   getString(R.string.desc_noroutes));
        } else if (routes.size() > 1){
            fabMenu.setVisibility(View.VISIBLE);
            int max = Math.min(3, routes.size()); // No máximo 3 rotas
            for (int i = 0; i < max; i++) {
                FloatingActionButton fabButton = new FloatingActionButton(getBaseContext());
                fabButton.setImageResource(R.drawable.ic_route);
                fabButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            getRouteFromServer(routes.get(v.getId()).getId_routes());
                            Log.e(MapActivity.TAG, "Rota " + selectedRoute.getId_routes() + " selecionada.");
                            drawRouteOnMap(selectedRoute);
                            getBusesOnRoute(selectedRoute.getId_routes());
                            if (fabMenu.isOpened()) {
                                fabMenu.toggle(true);
                            }
                        } catch (Exception e){
                            Log.e(MapActivity.TAG, e.toString());
                        }
                    }
                });
                fabButton.setLabelText("Rota " + (i + 1));
                fabButton.setId(i);
                fabButton.setButtonSize(FloatingActionButton.SIZE_MINI);
                fabMenu.addMenuButton(fabButton);
            }
        }
    }

    /**
     * Busca no servidor as informações sobre os ônibus de uma determinada rota
     * @param id Identificação da rota
     */
    public void getBusesOnRoute(int id) {
        String url = serverPrefix + "/routes/" + id + "/buses?localizations=1";
        JsonArrayRequest jreq = new CustomJsonArrayRequest(JsonArrayRequest.Method.GET, url, null, token,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONParser parser = new JSONParser();
                        Log.d("ONIBUS", "Recebidas informacoes do servidor");
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject ob = response.getJSONObject(i);
                                Bus b = parser.parseBus(ob);
                                if(buses.contains(b)){
                                    Log.d("ONIBUS", "Marcador existe, sera atualizado.");
                                    updateBusOnMap(b);
                                } else {
                                    buses.add(b);
                                    markBusOnMap(b);
                                    Log.d("ONIBUS", "Sem marcador associado para o onibus, um novo sera criado.");
                                }
                            }
                            // markBusesOnMap();
                        } catch (Exception e) {
                            Log.e("ONIBUS", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(MapActivity.TAG, error.toString());
                    }
                });
        requestQueue.add(jreq);
    }

    /**
     * Desenha as rotas e armazena os polylines associados
     */
    public void drawRouteOnMap(Route route){
        Polyline p;
        if (!route.isActiveOnMap()) {
            cleanMap();
            p = mMap.addPolyline(
                    new PolylineOptions()
                            .addAll(route.getPoints())
                            .clickable(true)
                            .color(ContextCompat.getColor(
                                    getApplicationContext(),
                                    R.color.colorPrimary))
            );
        } else {
            p = route.getAssociatedPolyline();
        }
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for(LatLng l : p.getPoints()){
            b.include(l);
        }
        LatLngBounds bounds = b.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));
        setTitleAndDescription(route.getName(), route.getDescription());
        route.setAssociatedPolyline(p);
    }

    /**
     * Limpa o mapa, removendo todas as rotas que possam estar visíveis
     */
    private void cleanMap(){
        for(Route r : routes){
            Polyline p = r.getAssociatedPolyline();
            if(p != null){
                p.remove();
            }
        }
    }

    /**
     * Marca os ônibus no mapa de acordo com os que estão ativos, dado o vetor buses
     * que é preenchido no recebimento da informação do servidor na funçao getBusesOnRoute.
     */
    public void markBusOnMap(Bus b) {
        Marker m = mMap.addMarker(
                new MarkerOptions()
                        .position(b.getCoordinates())
                        .zIndex(500)
                        .title("Ônibus " + b.getId())
        );
        m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.marcador));
        m.hideInfoWindow();
        LatLng local = b.getCoordinates();
        m.setSnippet(getAdressFromLocation(local.latitude,local.longitude));
        b.setAssociatedMarker(m);

        // Verifica se o ônibus da iteração atual é o que foi clicado, pra atualizar o textview
        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        if(lastClickWasABus && pref.getString(getString(R.string.clicked_bus), "null").equals(m.getTitle()))
            setTitleAndDescription("Ônibus " + b.getId(), m.getSnippet(), b.getLastUpdate());  // possível solução?
    }

    /**
     * Atualiza os ônibus no mapa
     */
    public void updateBusOnMap(Bus b) {
        Bus busOnMap = buses.get(buses.indexOf(b));
        LatLng coordinates = b.getCoordinates();
        Marker m = busOnMap.getAssociatedMarker();
        String address = getAdressFromLocation(coordinates.latitude, coordinates.longitude);
        m.setSnippet(address);
        busOnMap.setCoordinates(coordinates);

        // Verifica se o ônibus da iteração atual é o que foi clicado, pra atualizar o textview
        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        if(lastClickWasABus && pref.getString(getString(R.string.clicked_bus), "null").equals(m.getTitle()))
            setTitleAndDescription("Ônibus " + b.getId(), m.getSnippet(), b.getLastUpdate());
    }

    /**
     * Atualizar os ônibus automaticamente no mapa
     */
    Runnable updateBus = new Runnable(){
        @Override
        public void run(){
            if(selectedRoute != null) {
                getBusesOnRoute(selectedRoute.getId_routes());
            }
            // Verifica o tempo de atualização definido pelo usuário em configurações
            int update_time = pref.getInt(getString(R.string.update_time), 3);
            handler.postDelayed(updateBus, (update_time * 1000));
        }
    };

    /**
     * Fornece um manipulador para o objeto do mapa
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnMarkerClickListener(this);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        Marker ufc_mucambinho = googleMap.addMarker(
                new MarkerOptions()
                .position(new LatLng(-3.6932625,-40.3553395))
                .title("UFC Mucambinho")
                .anchor(0.5f, 0f)
        );
        ufc_mucambinho.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ufc_mucambinho_inv));

        Marker ufc_famed = googleMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(-3.6818927,-40.337298))
                        .title("UFC FAMED")
        );
        ufc_famed.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ufc_famed_small));

        Marker arco = googleMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(-3.6856522,-40.3443335))
                        .title("Arco N. Sra. de Fátima")
                        .anchor(0.5f, 0f)
        );
        arco.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.arco_small_inv));

        Marker mercado = googleMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(-3.686351,-40.3529675))
                        .title("Mercado Central")
        );
        mercado.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.mercado_small));

        Marker ced = googleMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(-3.6832558,-40.3409093))
                        .title("Centro de Educação à Distância")
        );
        ced.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ced_small));

        // Posiciona o mapa em Sobral
        // DEPRECATED - Atributos são definidos via XML
        //LatLng sobral = new LatLng(-3.6906438,-40.3503957);
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sobral, 15));
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
        startActivity(new Intent(MapActivity.this, SettingsActivity.class));
    }

    public void onClickNotifications(MenuItem item){
        Log.d("MAP_LOCATION", mMap.getCameraPosition().toString());
        startActivity(new Intent(MapActivity.this, NotificationListActivity.class));
    }

    @Override
    public void onResume(){
        super.onResume();
        handler.postDelayed(updateBus, 350);
    }

    /**
     * Funções para garantir que o thread updateBus pare quando o usuário
     * sair da activity do Mapa
     */
    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(updateBus);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(updateBus);
    }

    /**
     * Ações ao clicar em um marcador. Ele pega o título do marcador, que contém o ID do ônibus,
     * e salva numa sharedpreferences, pra verificar lá em cima qual é o ônibus que foi clicado
     * e deve ter endereço atualizado no textview em MacActivity
     * @param marker Marcador clicado
     * @return Booleano para verificar sucesso
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTitle().contains("Ônibus")) {
            SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(getString(R.string.clicked_bus), marker.getTitle());
            edit.apply();
            setTitleAndDescription(marker.getTitle(), marker.getSnippet());
            lastClickWasABus = true;
            imageInfo.setVisibility(View.GONE);
        } else {
            setTitleAndDescription(marker.getTitle(), "Ponto de Referência");
            imageInfo.setVisibility(View.VISIBLE);
            lastClickWasABus = false;
            switch (marker.getTitle()){
                case "UFC Mucambinho":
                    imageInfo.setBackgroundResource(R.drawable.mucambinho_round);
                    break;
                case "UFC FAMED":
                    imageInfo.setBackgroundResource(R.drawable.famed_round);
                    break;
                case "Arco N. Sra. de Fátima":
                    imageInfo.setBackgroundResource(R.drawable.arco_round);
                    break;
                case "Mercado Central":
                    imageInfo.setBackgroundResource(R.drawable.mercado_round);
                    break;
                case "Centro de Educação à Distância":
                    imageInfo.setBackgroundResource(R.drawable.ced_round);
                    break;
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
        return true;
    }

    /**
     * Pega o endereço do ônibus a partir de suas coordenadas
     * @param latitude latitude do ônibus
     * @param longitude longitude do ônibus
     * @return String com o endereço do ônibus
     */
    private String getAdressFromLocation(double latitude, double longitude){
        Log.d("LOCAL", "Obtendo informacoes do endereco");
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        String result = "Não foi possível localizar o endereço.";
        try{
            Address endereco = geocoder.getFromLocation(latitude, longitude, 1).get(0);
            result = endereco.getAddressLine(0);
        } catch (Exception e){
            Log.e(MapActivity.TAG, e.toString());
        }
        return result;
    }
}
