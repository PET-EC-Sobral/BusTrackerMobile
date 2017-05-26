package ufc.pet.bustracker.tools;

/**
 * Created by Isaben on 17/05/2017.
 */

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ufc.pet.bustracker.R;

/**
 * Listener responsável por ouvir as atualizações da localização do usuário,
 * caso este decida ativar o GPS.
 * Responsável também por calcular a distância do usuário ao ônibus clicado.
 */

public class CustomLocationListener implements LocationListener{

    private Location user_location;
    private GoogleMap map;
    private Marker user_marker;

    public CustomLocationListener(GoogleMap map){
        user_location = null;
        user_marker = null;
        this.map = map;
    }

    public void onLocationChanged(Location location){
        user_location = location;
        if(user_marker == null){
            user_marker = map.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zIndex(500)
                            .title("Usuário")
            );
            user_marker.setSnippet("Usuário");
            user_marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.marcador_pessoa));
        }
        else{
            user_marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }


    }

    public void onProviderEnabled(String provider){
        Log.d("LOCATION LISTENER", "ATIVADO!");
    }

    public void onProviderDisabled(String provider){
        Log.d("LOCATION LISTENER", "DESATIVADO!");
        if(user_marker != null){
            user_marker.remove();
            user_marker = null;
            user_location = null;
        }

    }

    public double distance(LatLng bus){
        Location bus_location = new Location("");
        bus_location.setLatitude(bus.latitude);
        bus_location.setLongitude(bus.longitude);

        return bus_location.distanceTo(user_location);
    }

    public void onStatusChanged(String provider, int status, Bundle extras){
        Log.d("STATUS CHANGED", "Do nothing");
    }
}
