package evil.eyes.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import evil.eyes.R;
import evil.eyes.core.adapters.CallAnalysisResultAdapter;
import thundersharp.sensivisionhealth.loganalyzer.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.asyncs.CallLogsAnalyzer;
import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;
import thundersharp.sensivisionhealth.loganalyzer.core.LogAnalyzerStarter;
import thundersharp.sensivisionhealth.loganalyzer.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;

public class CallLogAnalyserUi extends AppCompatActivity {

    private String timeStamp;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private boolean f = false;
    private TextView entries_log,tv,sortBy,talkTime,most_cont;
    private RelativeLayout progress_cont;
    private RecyclerView recyclerView;

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
        progress_cont = findViewById(R.id.progress_cont);
        most_cont = findViewById(R.id.most_cont);
        recyclerView = findViewById(R.id.recyclerView);

        progress_cont.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() ->
                LogAnalyzerStarter
                        .getCallLogsAnalyzer()
                        .setOperationMode(OperationModes.basicAnalyze)
                        .setArrangeBy(ArrangeBy.duration)
                        .setDataToProcess(data)
                        .analyze(new OnCallLogsAnalyzed() {
                            @Override
                            public void onExtractionSuccessFull(JSONObject data) {
                                runOnUiThread(() -> {
                                    try {
                                        updateUI(data);
                                    } catch (Exception e) {
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
                        }) , 1000);

    }

    private void updateUI(JSONObject data) throws Exception {
        String totalTTH = data.getString(JSONConstants.TOTAL_TALK_TIME).substring(0,data.getString(JSONConstants.TOTAL_TALK_TIME).indexOf(" h"));
        tv.setText("Completed...");
        sortBy.setText("Call "+data.getString(JSONConstants.QUERY_TYPE).toLowerCase());
        talkTime.setText("> "+totalTTH+"hrs");
        new Handler().postDelayed(()->progress_cont.setVisibility(View.INVISIBLE),500);

        String time = data.getJSONArray(JSONConstants.RECORDS).getJSONObject(0).getString(JSONConstants.DURATION);
        double hrs = Double.parseDouble(time.substring(0,time.indexOf(" h")));
        most_cont.setText("Most called:" +data.getString(JSONConstants.MOST_CONTACTED)+", "+Math.round((hrs/Double.parseDouble(totalTTH)) * 100)+"% of total Talk time");

        Log.e("TT",""+data.getJSONArray(JSONConstants.RECORDS));
        Log.e("FN",data.getJSONArray(JSONConstants.RECORDS).getJSONObject(0).getString(JSONConstants.NUMBER));

        recyclerView.setLayoutManager(new LinearLayoutManager(CallLogAnalyserUi.this));
        recyclerView.setAdapter(new CallAnalysisResultAdapter(data.getJSONArray(JSONConstants.RECORDS),timeStamp));

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