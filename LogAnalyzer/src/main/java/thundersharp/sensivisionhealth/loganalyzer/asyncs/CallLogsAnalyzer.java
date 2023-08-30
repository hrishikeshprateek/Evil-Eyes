package thundersharp.sensivisionhealth.loganalyzer.asyncs;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import thundersharp.sensivisionhealth.loganalyzer.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;
import thundersharp.sensivisionhealth.loganalyzer.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;
import thundersharp.sensivisionhealth.loganalyzer.models.GeneralLogOutput;
import thundersharp.sensivisionhealth.loganalyzer.utils.TimeUtil;

/**
 * @author hrishikeshprateek
 * An asynchronous task that analyzes call logs and generates statistics.
 * @apiNote Usage<br>
 * CallLogsAnalyzer
 * .getCallLogsAnalyzer()
 * .setArrangeBy(<b>{ArrangeBy}</b>)
 * .setOperationMode(<b>{OperationModes}</b>)
 * .execute(<b>{DATA}</b>)
 */
public class CallLogsAnalyzer extends AsyncTask<String, Void, String> {

    private OnCallLogsAnalyzed onCallLogsAnalyzed;
    private @ArrangeBy int arrangeBy;
    private String queryPhoneNo;
    private @OperationModes Integer operationMode;


    public static CallLogsAnalyzer getCallLogsAnalyzer() {
        return new CallLogsAnalyzer();
    }

    /**
     * @param onCallLogsAnalyzed instance of OnCallLogsAnalyzed Listener which returns <br>
     *                           <b>void onExtractionSuccessFull(JSONObject data);</b><br>
     *                           <b>void onFailedToAnalyze(AnalyzeException analyzeException);</b><br>
     *                           <b>void onProgress(int processed, long total);</b><br>
     * @return Returns back the instance of the same initialized class
     * @see OnCallLogsAnalyzed see the main OnCallLogsAnalyzed interface class docuentation for more info
     */
    public CallLogsAnalyzer setOnCallLogsAnalyzedListener(OnCallLogsAnalyzed onCallLogsAnalyzed) {
        this.onCallLogsAnalyzed = onCallLogsAnalyzed;
        return this;
    }

    /**
     * Sets the operation mode for call log analysis. it can be either <br>
     * <b>1. OperationModes.basicAnalyze</b><br>
     * <b>2. OperationModes.queryByNumber</b><br>
     *
     * @param operationMode The operation mode to be set.
     * @return The instance of CallLogsAnalyzer.
     * @see OperationModes The OperationModes enum class click here.
     */
    public CallLogsAnalyzer setOperationMode(@OperationModes int operationMode) {
        this.operationMode = operationMode;
        return this;
    }

    /**
     * Sets the arrangement mode for call log analysis results. It can be one among the two : <br>
     * <b>ArrangeBy.count</b><br>
     * <b>ArrangeBy.duration</b><br>
     *
     * @param arrangeBy The arrangement mode to be set.
     * @return The instance of CallLogsAnalyzer.
     * @see ArrangeBy The ArrangeBy enum class click here.
     */
    public CallLogsAnalyzer setArrangeBy(@ArrangeBy int arrangeBy) {
        this.arrangeBy = arrangeBy;
        return this;
    }

    /**
     * Sets the phone number to query for call log analysis.
     *
     * @param phoneNo The phone number to be set for querying.
     * @return The instance of CallLogsAnalyzer.
     * @throws IllegalArgumentException If the operation mode is not set properly.
     */
    public CallLogsAnalyzer setQueryPhoneNo(String phoneNo) {
        if (operationMode == null || operationMode == OperationModes.basicAnalyze) {
            throw new IllegalArgumentException("Either operationMode() is not called or is not set to OperationModes.queryByNumber !");
        }
        this.queryPhoneNo = phoneNo;
        return this;
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

    private String removeContryCode(String number) {
        return number.replaceFirst("^\\+91", "");
    }

    private boolean isIncoming(JSONObject individualCallRecord) throws JSONException {
        return !individualCallRecord.has("CALL_TYPE") || individualCallRecord.getString("CALL_TYPE").equalsIgnoreCase("INCOMING");
    }

    private boolean isOutgoing(JSONObject individualCallRecord) throws JSONException {
        return !individualCallRecord.has("CALL_TYPE") || individualCallRecord.getString("CALL_TYPE").equalsIgnoreCase("OUTGOING");
    }

    private boolean isMissed(JSONObject individualCallRecord) throws JSONException {
        return !individualCallRecord.has("CALL_TYPE") || individualCallRecord.getString("CALL_TYPE").equalsIgnoreCase("MISSED");
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
