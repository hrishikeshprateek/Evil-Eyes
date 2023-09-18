package evil.eyes.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import evil.eyes.R;
import thundersharp.sensivisionhealth.loganalyzer.core.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.core.LogAnalyzerStarter;
import thundersharp.sensivisionhealth.loganalyzer.core.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;

public class IndivisualAnalysis extends AppCompatActivity {

    private String timeStamp,number;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private boolean f = false;
    private TextView tv;
    private RelativeLayout progress_cont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indivisual_analysis);

        timeStamp = getIntent().getStringExtra("data");
        number = getIntent().getStringExtra("phone");

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String dataTrans = sharedPreferences.getString(timeStamp, null);
        if (timeStamp == null || dataTrans == null || number == null) finish();
        progressBar = findViewById(R.id.progressBare);
        progress_cont = findViewById(R.id.progress_contt);
        tv = findViewById(R.id.tvwU);

        Log.e("DATA_TRANS", dataTrans.toString());
        LogAnalyzerStarter
                .getCallLogsAnalyzer()
                .setOperationMode(OperationModes.queryByNumber)
                .setQueryPhoneNo(number)
                .setDataToProcess(dataTrans)
                .analyze(new OnCallLogsAnalyzed() {
                    @Override
                    public void onExtractionSuccessFull(JSONObject data) {
                        runOnUiThread(() -> {
                            Toast.makeText(IndivisualAnalysis.this, "Done !!", Toast.LENGTH_SHORT).show();
                            progress_cont.setVisibility(View.GONE);
                            ((TextView) findViewById(R.id.TEST)).setText(data.toString());
                        });
                    }

                    @Override
                    public void onFailedToAnalyze(AnalyzeException analyzeException) {
                        runOnUiThread(() -> Toast.makeText(IndivisualAnalysis.this, ""+ analyzeException.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onProgress(int processed, long total) {
                        runOnUiThread(() -> {
                            if (!f) {
                                progressBar.setMax((int) total);
                                //entries_log.setText((int) total+" entries");
                                f = true;
                            }
                            progressBar.setProgress(processed);
                            tv.setText("Found " + processed + " numbers of " + total + " entries");
                        });

                    }
                });
        //generateGraph();
    }

    public void close(View view) {
        finish();
    }

    private void generateGraph(){
        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.progressBar));

        Pie pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(IndivisualAnalysis.this, event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Apples", 6371664));
        data.add(new ValueDataEntry("Pears", 789622));
        data.add(new ValueDataEntry("Bananas", 7216301));
        data.add(new ValueDataEntry("Grapes", 1486621));
        data.add(new ValueDataEntry("Oranges", 1200000));

        pie.data(data);

        pie.title("Fruits imported in 2015 (in kg)");

        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("Retail channels")
                .padding(0d, 0d, 10d, 0d);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        anyChartView.setChart(pie);
    }
}