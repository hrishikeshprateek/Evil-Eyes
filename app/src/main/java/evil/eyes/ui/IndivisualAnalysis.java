package evil.eyes.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Align;
import com.anychart.enums.Anchor;
import com.anychart.enums.LegendLayout;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import evil.eyes.R;
import evil.eyes.core.adapters.IndivisualDayCallLogAdapter;
import evil.eyes.core.annos.LineChartModes;
import evil.eyes.core.anychart.LineDataPoints;
import evil.eyes.core.anychart.LineDataPointsSingle;
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
    private RecyclerView dailyDataRecordRecycler;

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
        dailyDataRecordRecycler = findViewById(R.id.dailyDataRecord);

        tv = findViewById(R.id.tvwU);

        //Log.e("DATA_TRANS", dataTrans.toString());
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
        renderLineChart(LineChartModes.MODE_DEFAULT,data);
        updateDailyRecord(data.getJSONArray(JSONConstants.DAILY_RECORDS));

    }

    private void updateDailyRecord(JSONArray jsonArray) throws Exception {
        dailyDataRecordRecycler.setAdapter(new IndivisualDayCallLogAdapter(jsonArray));
    }

    AnyChartView line;
    private void renderLineChart(@LineChartModes int lineChartMode, JSONObject data) throws Exception {
        line = findViewById(R.id.any_chart_view_line);
        APIlib.getInstance().setActiveAnyChartView(line);
        //line.removeAllViews();
        Cartesian cartesian = AnyChart.line();

        cartesian.credits(false);
        cartesian.xZoom(0);
        cartesian.xScroller(true);

        cartesian.animation(true);
        cartesian.padding(10d, 20d, 5d, 20d);
        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.FLOAT);

        cartesian.title("No of times called per day");

        cartesian.xAxis(0).title("Dates of call Events");
        cartesian.yAxis(0).labels().padding(5d, 5d, 5d, 5d);

        List<DataEntry> seriesData = new ArrayList<>();
        for (int i=0; i < data.getJSONArray(JSONConstants.DAILY_RECORDS).length(); i++){
            JSONObject individualData = data.getJSONArray(JSONConstants.DAILY_RECORDS).optJSONObject(i);
            if (lineChartMode == LineChartModes.MODE_DEFAULT)
                seriesData.add(new LineDataPoints(individualData.getString(JSONConstants.CALL_DATE),
                        individualData.getInt(JSONConstants.INCOMING_COUNT),
                        individualData.getInt(JSONConstants.OUTGOING_COUNT),
                        individualData.getInt(JSONConstants.MISSED_COUNT),
                        individualData.getInt(JSONConstants.DURATION)));
            else if (lineChartMode == LineChartModes.MODE_INCOMING) {
                seriesData
                        .add(new LineDataPointsSingle(individualData.getString(JSONConstants.CALL_DATE),
                                individualData.getInt(JSONConstants.INCOMING_COUNT),
                                individualData.getInt(JSONConstants.DURATION)));
            } else if (lineChartMode == LineChartModes.MODE_OUTGOING) {
                seriesData
                        .add(new LineDataPointsSingle(individualData.getString(JSONConstants.CALL_DATE),
                                individualData.getInt(JSONConstants.OUTGOING_COUNT),
                                individualData.getInt(JSONConstants.DURATION)));
            } else if (lineChartMode == LineChartModes.MODE_MISSED) {
                seriesData
                        .add(new LineDataPointsSingle(individualData.getString(JSONConstants.CALL_DATE),
                                individualData.getInt(JSONConstants.MISSED_COUNT),
                                individualData.getInt(JSONConstants.DURATION)));
            }
        }

        Set set = Set.instantiate();
        set.data(seriesData);

        if (lineChartMode == LineChartModes.MODE_DEFAULT){

            Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
            Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
            Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");
            Mapping duration = set.mapAs("{ x: 'x', value: 'value4' }");

            Line series1 = cartesian.line(series1Mapping);
            series1.name("Incoming");
            series1.hovered().markers().enabled(true);
            series1.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series1.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series2 = cartesian.line(series2Mapping);
            series2.name("Outgoing");
            series2.hovered().markers().enabled(true);
            series2.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series2.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series3 = cartesian.line(series3Mapping);
            series3.name("Missed");
            series3.hovered().markers().enabled(true);
            series3.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series3.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series4 = cartesian.line(duration);
            series4.name("Duration");
            series4.hovered().markers().enabled(true);
            series4.hovered().markers()
                    .type(MarkerType.PENTAGON)
                    .size(4d);
            series4.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

        } else {
            Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
            Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");

            Line series1 = cartesian.line(series1Mapping);

            if (lineChartMode == LineChartModes.MODE_INCOMING){
                series1.name("Incoming");
            } else if (lineChartMode == LineChartModes.MODE_OUTGOING) {
                series1.name("Outgoing");

            } else if (lineChartMode == LineChartModes.MODE_MISSED) {
                series1.name("Missed");
            }

            series1.hovered().markers().enabled(true);
            series1.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series1.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

            Line series4 = cartesian.line(series2Mapping);
            series4.name("Duration");
            series4.hovered().markers().enabled(true);
            series4.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4d);
            series4.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5d)
                    .offsetY(5d);

        }
        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);
        line.setChart(cartesian);
    }

    public void close(View view) {
        finish();
    }

    private void generateGraph(String date, JSONObject dataRecord) throws JSONException {
        AnyChartView anyChartView = findViewById(R.id.any_chart_view);

        APIlib.getInstance().setActiveAnyChartView(anyChartView);
        Pie pie = AnyChart.pie();
        //anyChartView.setBackgroundColor("#FFEFEFEF");
        pie.background().enabled(true).fill("rgb(239,239,239)");
        pie.credits(false);
        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                //Toast.makeText(IndivisualAna``lysis.this, event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
                try {
                /*    if (event.getData().get("x").equalsIgnoreCase("incoming"))
                        renderLineChart(LineChartModes.MODE_INCOMING, dataRecord);
                    else if (event.getData().get("x").equalsIgnoreCase("outgoing"))
                        renderLineChart(LineChartModes.MODE_OUTGOING, dataRecord);
                    else if (event.getData().get("x").equalsIgnoreCase("missed")) {
                        line.invalidate();
                        renderLineChart(LineChartModes.MODE_MISSED, dataRecord);
                    }*/
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }

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