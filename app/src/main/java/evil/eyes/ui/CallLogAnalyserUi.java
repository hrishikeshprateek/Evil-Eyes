package evil.eyes.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import evil.eyes.R;
import thundersharp.sensivisionhealth.loganalyzer.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.asyncs.CallLogsAnalyzer;
import thundersharp.sensivisionhealth.loganalyzer.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;

public class CallLogAnalyserUi extends AppCompatActivity {

    private String timeStamp;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private boolean f = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log_analyser_ui);

        timeStamp = getIntent().getStringExtra("data");
        sharedPreferences = getSharedPreferences("data",MODE_PRIVATE);
        String data = sharedPreferences.getString(timeStamp,null);
        if (timeStamp == null || data == null) finish();

        progressBar =findViewById(R.id.progressBar);
        TextView tv = findViewById(R.id.tvw);

        new Handler().postDelayed(()->{
            CallLogsAnalyzer
                    .getCallLogsAnalyzer()
                    .setOperationMode(OperationModes.basicAnalyze)
                    .setArrangeBy(ArrangeBy.duration)
                    .setOnCallLogsAnalyzedListener(new OnCallLogsAnalyzed() {
                        @Override
                        public void onExtractionSuccessFull(JSONObject data) {
                            runOnUiThread(()->tv.setText("complete..."));
                        }

                        @Override
                        public void onFailedToAnalyze(AnalyzeException analyzeException) {
                            runOnUiThread(()-> Toast.makeText(CallLogAnalyserUi.this, analyzeException.getMessage(), Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onProgress(int processed, long total) {
                            runOnUiThread(()->{
                                if (!f){
                                    progressBar.setMax((int) total);
                                    f = true;
                                }
                                progressBar.setProgress(processed);
                                tv.setText("Processed "+processed+" of "+total+" entries");
                            });

                        }
                    }).execute(data);

        },1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Successfully cleaned up the memory", Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(timeStamp);
        editor.apply();
    }
}