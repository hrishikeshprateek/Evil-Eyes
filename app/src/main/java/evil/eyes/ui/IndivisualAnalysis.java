package evil.eyes.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.fonts.FontStyle;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import evil.eyes.R;
import evil.eyes.core.utils.TimeUtils;
import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;
import thundersharp.sensivisionhealth.loganalyzer.core.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.core.LogAnalyzerStarter;
import thundersharp.sensivisionhealth.loganalyzer.core.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;

public class IndivisualAnalysis extends AppCompatActivity {

    private String timeStamp,number;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private boolean f = false;
    private TextView tv,name_user,phoneNoUser,first_contact;
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
        name_user = findViewById(R.id.name_user);
        phoneNoUser = findViewById(R.id.phoneNoUser);
        first_contact = findViewById(R.id.first_contact);

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
                            try {
                                updateUI(data);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(IndivisualAnalysis.this, " "+e.getMessage() , Toast.LENGTH_SHORT).show();
                            }

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

    private void updateUI(JSONObject data) throws Exception {
        progress_cont.setVisibility(View.GONE);
        String date = TimeUtils.formatDateWithSuffix(
                data.getJSONArray(JSONConstants.DAILY_RECORDS)
                        .getJSONObject(0)
                        .getString(JSONConstants.CALL_DATE));
        ((TextView) findViewById(R.id.TEST)).setText(data.toString());
        name_user.setText(data.getString(JSONConstants.NAME_SAVED));
        phoneNoUser.setText(data.getString(JSONConstants.QUERY_NUMBER));
        first_contact.setText("First Contacted on "+ date);


        generateGraph(date,data);



    }

    public void close(View view) {
        finish();
    }

    private void generateGraph(String date, JSONObject dataRecord) throws JSONException {
        AnyChartView anyChartView = findViewById(R.id.any_chart_view);

        Pie pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(IndivisualAnalysis.this, event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Outgoing", dataRecord.getInt(JSONConstants.OUTGOING_COUNT_OUT)));
        data.add(new ValueDataEntry("Incoming", dataRecord.getInt(JSONConstants.INCOMING_COUNT_OUT)));
        data.add(new ValueDataEntry("Missed", dataRecord.getInt(JSONConstants.MISSED_COUNT_OUT)));

        pie.data(data);

        /*pie.title("Call Statistics");
        pie.title().fontColor("#000");*/
        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("Data from "+ date)
                .padding(0d, 0d, 10d, 0d);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        anyChartView.setChart(pie);
    }
}