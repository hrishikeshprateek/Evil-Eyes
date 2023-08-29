package evil.eyes.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import evil.eyes.R;
import thundersharp.sensivisionhealth.loganalyzer.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.asyncs.CallLogsAnalyzer;
import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;
import thundersharp.sensivisionhealth.loganalyzer.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;

public class CallLogAnalyserUi extends AppCompatActivity {

    private String timeStamp;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private boolean f = false;
    private TextView entries_log,tv,sortBy,talkTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log_analyser_ui);

        timeStamp = getIntent().getStringExtra("data");
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String data = sharedPreferences.getString(timeStamp, null);
        if (timeStamp == null || data == null) finish();

        progressBar = findViewById(R.id.progressBar);
        entries_log = findViewById(R.id.entries_log);
        tv = findViewById(R.id.tvw);
        sortBy = findViewById(R.id.sortBy);
        talkTime = findViewById(R.id.talkTime);

        new Handler().postDelayed(() ->
                CallLogsAnalyzer
                        .getCallLogsAnalyzer()
                        .setOperationMode(OperationModes.basicAnalyze)
                        .setArrangeBy(ArrangeBy.duration)
                        .setOnCallLogsAnalyzedListener(new OnCallLogsAnalyzed() {
                            @Override
                            public void onExtractionSuccessFull(JSONObject data) {
                                runOnUiThread(() -> {
                                    try {
                                        updateUI(data);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }

                            @Override
                            public void onFailedToAnalyze(AnalyzeException analyzeException) {
                                runOnUiThread(() -> Toast.makeText(CallLogAnalyserUi.this, analyzeException.getMessage(), Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onProgress(int processed, long total) {
                                runOnUiThread(() -> {
                                    if (!f) {
                                        progressBar.setMax((int) total);
                                        entries_log.setText((int) total+" entries");
                                        f = true;
                                    }
                                    progressBar.setProgress(processed);
                                    tv.setText("Processed " + processed + " of " + total + " entries");
                                });

                            }
                        }).execute(data), 1000);

    }

    private void updateUI(JSONObject data) throws JSONException {
        tv.setText("Completed...");
        sortBy.setText("Call "+data.getString(JSONConstants.QUERY_TYPE).toLowerCase());
        talkTime.setText("> "+data.getString(JSONConstants.TOTAL_TALK_TIME).substring(0,data.getString(JSONConstants.TOTAL_TALK_TIME).indexOf("h"))+"hrs");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Successfully cleaned up the memory", Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(timeStamp);
        editor.apply();
    }

    public void close(View view) {
        finish();
    }
}