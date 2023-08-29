package evil.eyes.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import evil.eyes.R;
import evil.eyes.core.adapters.CallLogAdapter;

public class PhoneDisplay extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppCompatButton analyze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_display);
        ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(h -> finish());
        recyclerView = findViewById(R.id.rec);
        analyze = findViewById(R.id.schedule_new);

        if (getIntent().getStringExtra("uri") == null) {
            finish();
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Please wait processing retrieved logs...")
                .setCancelable(false)
                .show();
        String URL = getIntent().getStringExtra("uri");

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, response -> {
                try {
                    JSONArray jsonObject = new JSONArray(response);
                    recyclerView.setAdapter(new CallLogAdapter(jsonObject));
                    analyze.setOnClickListener(o ->{
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        SharedPreferences sharedPreferences = getSharedPreferences("data",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(timeStamp,response);
                        editor.apply();
                        startActivity(new Intent(PhoneDisplay.this, CallLogAnalyserUi.class).putExtra("data",timeStamp));
                    });
                    alertDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    alertDialog.dismiss();
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    alertDialog.dismiss();
                }
            }) {

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