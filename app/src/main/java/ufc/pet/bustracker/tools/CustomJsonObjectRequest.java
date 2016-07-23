package ufc.pet.bustracker.tools;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Isaben on 22/07/2016.
 */
public class CustomJsonObjectRequest extends JsonObjectRequest {
    private Map header;

    public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest, String token, Response.Listener listener, Response.ErrorListener errorListener)
    {
        super(method, url, jsonRequest, listener, errorListener);
        header = new HashMap();
        header.put("Token", token);
    }

    @Override
    public Map getHeaders() throws AuthFailureError {
        //adicionamos o token do usuario santana que é um administrador
        return header;
    }
}