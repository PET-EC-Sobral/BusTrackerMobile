package ufc.pet.bustracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import ufc.pet.bustracker.tools.CustomJsonObjectRequest;

public class SplashActivity extends AppCompatActivity {



    private SharedPreferences pref;
    private RequestQueue requestQueue;
    private String device_id;          // único para o dispositivo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        String token; // token do usuário

        if(!isOnline())
            exibir_alerta();
        else {
            pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
            token = pref.getString(getString(R.string.token), "null");
            requestQueue = Volley.newRequestQueue(getApplicationContext());

            if (token.equals("null"))
                getTokenIfExists();
            else {
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){

                        iniciar();
                    }
                }, 300);

            }
        }
    }

    /**
     * Verificar se o dispositivo está conectado com a internet
     * @return true se sim, false se não
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Exibe alerta caso não esteja conectado a internet
     */
    public void exibir_alerta(){
        AlertDialog.Builder alertD = new AlertDialog.Builder(this);

        alertD.setMessage("Cheque sua conexão com a internet e tente novamente!\nSe ainda assim tiver problemas, contate a equipe de desenvolvimento imediatamente no link a seguir\nhttp://www.pet.ec.ufc.br/contato");
        alertD.setTitle("Erro de Conexão!");
        alertD.setNeutralButton("OK", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                finish();
            }
        });
        alertD.show();
    }

    /**
     * Verifica no servidor se existe um token com o ID do dispostivo
     * Se não tiver, chama função de registro
     * Se tiver, inicia a aplicação
     */
    public void getTokenIfExists(){
        String server = getString(R.string.host_prefix) + "/users/tokens";
        JSONObject dados = null;
        try{
            dados = new JSONObject("{\"email\": \""+device_id+"\"," +
                                        "\"password\": \"dummy\"}");
        }
        catch(JSONException e){
            Log.e("JSON", "erro???");
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, server, dados,
                new Response.Listener<JSONObject>(){
                    String newToken;
                    @Override
                    public void onResponse(JSONObject Response){
                        try{
                            newToken = Response.getString("token");
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString(getString(R.string.token), newToken);
                        edit.apply();
                        iniciar();
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.e("Erro", "deu pau no excel");
                        registerUserToken();
                    }
                });
        requestQueue.add(request);
    }

    /**
     * Registra o dispositivo no servidor e inicia a aplicação
     */
    public void registerUserToken(){
        String server = getString(R.string.host_prefix) + "/users/";
        JSONObject dados = null;
        try{
            dados = new JSONObject("{\"name\": \"dummy_name\"," +
                                    "\"email\": \""+device_id+"\"," +
                                    "\"password\": \"dummy\"," +
                                    "\"permission\": 1}");
        }
        catch(JSONException e){
            Log.e("JSON registro", "errado");

        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, server, dados,
                new Response.Listener<JSONObject>(){
                    String newToken;
                    @Override
                    public void onResponse(JSONObject Response){
                        try{
                            newToken = Response.getString("token");
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString(getString(R.string.token), newToken);
                        edit.apply();
                        iniciar();
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.e("erro", "deu pau");
                    }
                });
        requestQueue.add(request);
    }

    /**
     * Função para iniciar a aplicação
     */
    public void iniciar(){
        Intent intent = new Intent(SplashActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public void sendToken(String token_firebase){
        String server = getString(R.string.host_prefix) + "/routes/86/messages";
        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        String token = pref.getString(getString(R.string.token), "null");
        Log.e("token send", token);
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
                        Log.e("Erro em Registro", "Vai lá ver, Firebase");
                    }
                });
        requestQueue.add(request);
    }

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }
}
