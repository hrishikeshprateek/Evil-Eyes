package evil.eyes.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import evil.eyes.R;
import thundersharp.sensivisionhealth.loganalyzer.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.asyncs.CallLogsAnalyzer;

public class IndivisualAnalysis extends AppCompatActivity {

    private String timeStamp,number;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indivisual_analysis);

        timeStamp = getIntent().getStringExtra("data");
        number = getIntent().getStringExtra("phone");

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String data = sharedPreferences.getString(timeStamp, null);
        if (timeStamp == null || data == null || number == null) finish();

        CallLogsAnalyzer
                .getCallLogsAnalyzer()
                .setOperationMode(OperationModes.queryByNumber)
                .setQueryPhoneNo(number);
    }
}