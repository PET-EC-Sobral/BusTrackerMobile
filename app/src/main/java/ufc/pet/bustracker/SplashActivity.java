package ufc.pet.bustracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private RequestQueue requestQueue;
    private String device_id;          // único para o dispositivo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AndroidThreeTen.init(this);

        device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        String token; // token do usuário

        if(!isOnline())
            exibir_alerta(R.string.erro_aparelho_offline_title,
                          R.string.erro_aparelho_offline_msg);
        else {
            pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
            token = pref.getString(getString(R.string.token), "null");
            requestQueue = Volley.newRequestQueue(getApplicationContext());

            if (token.equals("null"))
                getTokenIfExists();
            //Delay para a splash screen
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
     * Exibe alerta com título e mensagens definidos nos recursos
     */
    public void exibir_alerta(int titleID, int msgID){
        AlertDialog.Builder alertD = new AlertDialog.Builder(this);

        alertD.setMessage(msgID);
        alertD.setTitle(titleID);
        alertD.setNeutralButton(R.string.botao_ok, new DialogInterface.OnClickListener(){
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
        JSONObject dados = null;

        try{
            dados = new JSONObject("{\"email\": \""+device_id+"\"," +
                                        "\"password\": \"dummy\"}");
        }
        catch(JSONException e){
            Log.e("JSON", e.getMessage());
        }

        String url = getString(R.string.host_prefix) + "/users/tokens";
        Log.e("teste", "FUNCIONA PELO AMOR DE DEUS");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, dados,
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
                        Log.e("Registro", error.toString());
                        Log.d("Registro", "Este dispositivo (ID" + device_id +
                                          ") não está registrado no servidor. Iniciando tentativa" +
                                          " de registro");
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
            Log.e("JSON registro", e.getMessage());
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
                        exibir_alerta(R.string.erro_registro_aparelho_title,
                                      R.string.erro_registro_aparelho_msg);
                        Log.e("Registro", error.toString());
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

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }
}
