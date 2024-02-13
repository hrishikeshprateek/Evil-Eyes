package evil.eyes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import evil.eyes.ui.LoginActivity;
import thundersharp.sensivisionhealth.loganalyzer.core.LogAnalyzerStarter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            LogAnalyzerStarter.getCallLogsAnalyzer().releaseMemory(MainActivity.this);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        },2000);
    }
}