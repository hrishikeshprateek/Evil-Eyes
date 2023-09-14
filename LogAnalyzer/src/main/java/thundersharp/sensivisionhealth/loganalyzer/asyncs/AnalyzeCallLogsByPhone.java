package thundersharp.sensivisionhealth.loganalyzer.asyncs;

import android.os.AsyncTask;

import thundersharp.sensivisionhealth.loganalyzer.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;

public class AnalyzeCallLogsByPhone extends AsyncTask<String,Void,Void> {
    private final OnCallLogsAnalyzed onCallLogsAnalyzed;
    private final String queryPhoneNumber;

    public AnalyzeCallLogsByPhone(OnCallLogsAnalyzed onCallLogsAnalyzed, String queryPhoneNumber) {
        this.onCallLogsAnalyzed = onCallLogsAnalyzed;
        this.queryPhoneNumber = queryPhoneNumber;
    }

    public static AnalyzeCallLogsByPhone getCallLogsAnalyzerByPhone(OnCallLogsAnalyzed onCallLogsAnalyzed, String queryPhoneNumber) {
        return new AnalyzeCallLogsByPhone(onCallLogsAnalyzed, queryPhoneNumber);
    }

    @Override
    protected Void doInBackground(String... strings) {
        return null;
    }
}
