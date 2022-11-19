package evil.eyes.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import evil.eyes.R;
import evil.eyes.core.adapters.CallLogAdapter;

public class PhoneDisplay extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_display);
        recyclerView = findViewById(R.id.rec);

        if (getIntent().getStringExtra("uri") == null){
            finish();
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Please wait processing retrieved logs...")
                .setCancelable(false)
                .show();
        String URL = getIntent().getStringExtra("uri");

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray jsonObject = new JSONArray(response);
                        recyclerView.setAdapter(new CallLogAdapter(jsonObject));
                        alertDialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        alertDialog.dismiss();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    alertDialog.dismiss();
                }
            }){

                @Override
                public String getPostBodyContentType() {
                    return "application/json;charset=UTF-8";
                }


            };
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
            alertDialog.dismiss();
        }
    }
}