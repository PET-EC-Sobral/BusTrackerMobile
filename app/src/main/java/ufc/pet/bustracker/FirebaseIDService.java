package ufc.pet.bustracker;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import ufc.pet.bustracker.tools.CustomJsonObjectRequest;


/**
 * Classe para receber o ID do Firebase do usuário e salvar o mesmo
 * Created by Isaben on 05/10/2016.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {

    private RequestQueue requestQueue;
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e("Token", "Refreshed token: " + refreshedToken);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        sendToken(refreshedToken);
    }

    /**
     * Registra o usuário na lista de mensagens de uma rota para receber notificações
     * da mesma
     * @param token_firebase token
     */
    public void sendToken(String token_firebase){
        String server = getString(R.string.host_prefix) + "/routes/86/messages/register";
        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        String token = pref.getString(getString(R.string.token), "null");
        JSONObject dado = null;
        try{
            dado = new JSONObject("{\"registration_token_firebase\": \""+token_firebase+"\"}");
        }
        catch(JSONException e){
            Log.e("deu erro", "no token firebase");
        }
        JsonObjectRequest request = new CustomJsonObjectRequest(Request.Method.POST, server, dado, token,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject Response){
                        Log.i("Registro deu certo!", "No firebase pra mensanges");
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.e("Erro em Firebase", error.toString());
                    }
                });
        requestQueue.add(request);
    }
}
