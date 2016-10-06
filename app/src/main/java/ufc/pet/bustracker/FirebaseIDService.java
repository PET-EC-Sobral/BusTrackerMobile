package ufc.pet.bustracker;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;



/**
 * Classe para receber o ID do Firebase do usuário e salvar o mesmo
 * Created by Isaben on 05/10/2016.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e("Token", "Refreshed token: " + refreshedToken);

        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(getString(R.string.firebase), refreshedToken);
        edit.putBoolean(getString(R.string.firebase_on), false);
        edit.apply();
    }


}
