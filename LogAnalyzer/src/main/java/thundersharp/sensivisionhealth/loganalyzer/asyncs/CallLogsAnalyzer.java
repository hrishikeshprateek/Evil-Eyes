package thundersharp.sensivisionhealth.loganalyzer.asyncs;

import static thundersharp.sensivisionhealth.loganalyzer.core.helpers.CallLogsUtil.isIncoming;
import static thundersharp.sensivisionhealth.loganalyzer.core.helpers.CallLogsUtil.isMissed;
import static thundersharp.sensivisionhealth.loganalyzer.core.helpers.CallLogsUtil.isOutgoing;
import static thundersharp.sensivisionhealth.loganalyzer.core.helpers.CallLogsUtil.removeContryCode;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import thundersharp.sensivisionhealth.loganalyzer.core.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;
import thundersharp.sensivisionhealth.loganalyzer.core.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;
import thundersharp.sensivisionhealth.loganalyzer.models.GeneralLogOutput;
import thundersharp.sensivisionhealth.loganalyzer.utils.TimeUtil;

public class CallLogsAnalyzer extends AsyncTask<String, Void, String> {

    private final OnCallLogsAnalyzed onCallLogsAnalyzed;
    private final @ArrangeBy int arrangeBy;

    public static CallLogsAnalyzer getCallLogsAnalyzer(OnCallLogsAnalyzed onCallLogsAnalyzed, int arrangeBy) {
        return new CallLogsAnalyzer(onCallLogsAnalyzed, arrangeBy);
    }

    public CallLogsAnalyzer(OnCallLogsAnalyzed onCallLogsAnalyzed, int arrangeBy) {
        this.onCallLogsAnalyzed = onCallLogsAnalyzed;
        this.arrangeBy = arrangeBy;
    }

    /**
     * @param strings Takes the call logs json data as input.
     * @return Returns progress, error, analyzed data back.
     * @throws IllegalArgumentException IllegalArgumentException tells weather all the parameters are set successfully or not before execution.
     * @see IllegalArgumentException click here to read more about IllegalArgumentException class
     */
    @Override
    protected String doInBackground(String... strings) {
        String data = strings[0];

        if (onCallLogsAnalyzed == null || data.isEmpty())
            throw new IllegalArgumentException("onCallLogsAnalyzedListener() not set or data invalid !!");
        try {
            //Parses the call logs in the form JSONArray to Map
            List<GeneralLogOutput> callLogEntity = parseJsonCallLogs(new JSONArray(data));
            //Generates stats and is ready for output
            JSONObject output = generateStats(callLogEntity);
            //Output is dispatched via the interface
            onCallLogsAnalyzed.onExtractionSuccessFull(output);

        } catch (Exception e) {
            handleException(e);
        }
        return "com";
    }

    private List<GeneralLogOutput> parseJsonCallLogs(JSONArray callLogData) throws JSONException {
        Map<String, GeneralLogOutput> callLogEntity = new LinkedHashMap<>();
        for (int i = 0; i < callLogData.length(); i++) {
            onCallLogsAnalyzed.onProgress(i, callLogData.length());
            JSONObject individualCallRecord = callLogData.getJSONObject(i);
            if (individualCallRecord.has("NUMBER") && individualCallRecord.has("DURATION")) {
                updateLogEntry(individualCallRecord, callLogEntity);
            }
        }
        return new ArrayList<>(callLogEntity.values());
    }

    private void updateLogEntry(JSONObject individualCallRecord, Map<String, GeneralLogOutput> callLogEntity) throws JSONException {
        String phoneWithoutCountryCode = removeContryCode(individualCallRecord.getString("NUMBER"));
        boolean isIncoming = isIncoming(individualCallRecord);
        boolean isOutgoing = isOutgoing(individualCallRecord);
        boolean isMissed = isMissed(individualCallRecord);
        GeneralLogOutput output = callLogEntity.get(phoneWithoutCountryCode);

        if (output != null) {
            //Update entry
            callLogEntity.replace(phoneWithoutCountryCode,
                    new GeneralLogOutput(output.getNUMBER(),
                            output.getCALL_COUNT() + 1,
                            output.getDURATION() + Long.parseLong(individualCallRecord.getString("DURATION")),
                            output.getNAME(),
                            isIncoming ? (output.getINCOMING() + 1) : output.getINCOMING(),
                            isOutgoing ? (output.getOUTGOING() + 1) : output.getOUTGOING(),
                            isMissed ? (output.getMISSED() + 1) : output.getMISSED()));

        } else {
            //new Entry
            callLogEntity.put(phoneWithoutCountryCode,
                    new GeneralLogOutput(phoneWithoutCountryCode,
                            1,
                            Long.parseLong(individualCallRecord.getString("DURATION")),
                            individualCallRecord.getString("NAME"),
                            isIncoming ? 1 : 0,
                            isOutgoing ? 1 : 0,
                            isMissed ? 1 : 0));
        }
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        onCallLogsAnalyzed.onFailedToAnalyze(new AnalyzeException(e.getMessage(), e.getCause()));
    }

    private JSONObject generateStats(List<GeneralLogOutput> callLogEntity) throws JSONException {
        if (arrangeBy == ArrangeBy.count) {
            callLogEntity.sort(Comparator.comparing(GeneralLogOutput::getCALL_COUNT).reversed());
        } else {
            callLogEntity.sort(Comparator.comparing(GeneralLogOutput::getDURATION).reversed());
        }
        long totalTalkTime = 0;
        String mostCalledNumber = null;
        long mostCalledValue = 0;

        JSONArray out = new JSONArray();
        for (GeneralLogOutput logOutput : callLogEntity) {
            totalTalkTime += logOutput.getDURATION();

            long currentValue = (arrangeBy == ArrangeBy.duration) ?
                    logOutput.getDURATION() :
                    logOutput.getCALL_COUNT();

            if (currentValue > mostCalledValue) {
                mostCalledNumber = logOutput.getNUMBER();
                mostCalledValue = currentValue;
            }

            JSONObject jsonEntry = new JSONObject();
            jsonEntry.put(JSONConstants.NUMBER, logOutput.getNUMBER());
            jsonEntry.put(JSONConstants.DURATION, TimeUtil.convertSecondsToFormat(logOutput.getDURATION()));
            jsonEntry.put(JSONConstants.NAME, logOutput.getNAME());
            jsonEntry.put(JSONConstants.CALL_COUNT, logOutput.getCALL_COUNT());
            jsonEntry.put(JSONConstants.INCOMING_COUNT, logOutput.getINCOMING());
            jsonEntry.put(JSONConstants.OUTGOING_COUNT, logOutput.getOUTGOING());
            jsonEntry.put(JSONConstants.MISSED_COUNT, logOutput.getMISSED());
            out.put(jsonEntry);
        }

        JSONObject jsonObjectFinal = new JSONObject();
        jsonObjectFinal.put(JSONConstants.MOST_CONTACTED, mostCalledNumber);
        jsonObjectFinal.put(JSONConstants.TOTAL_TALK_TIME, TimeUtil.convertSecondsToFormat(totalTalkTime));
        jsonObjectFinal.put(JSONConstants.RECORDS, out);
        jsonObjectFinal.put(JSONConstants.QUERY_TYPE, arrangeBy == ArrangeBy.duration ? "DURATION" : "COUNT");
        return jsonObjectFinal;
    }
}
