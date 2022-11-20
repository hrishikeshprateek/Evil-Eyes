package evil.eyes.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import evil.eyes.R;
import evil.eyes.core.adapters.CallLogAdapter;
import evil.eyes.core.adapters.SmsLogAdapter;

public class SmsDisplay extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_display);

        ((Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(h->finish());
        recyclerView = findViewById(R.id.recJ);

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
                        recyclerView.setAdapter(new SmsLogAdapter(jsonObject));
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