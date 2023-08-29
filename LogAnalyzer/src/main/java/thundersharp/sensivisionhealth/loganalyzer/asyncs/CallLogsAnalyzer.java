package thundersharp.sensivisionhealth.loganalyzer.asyncs;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;

import thundersharp.sensivisionhealth.loganalyzer.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;
import thundersharp.sensivisionhealth.loganalyzer.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;
import thundersharp.sensivisionhealth.loganalyzer.models.GeneralLogOutput;

/**
 * @apiNote Usage<br>
 *             CallLogsAnalyzer
 *                  .getCallLogsAnalyzer()
 *                  .setArrangeBy(<b>{ArrangeBy}</b>)
 *                  .setOperationMode(<b>{OperationModes}</b>)
 *                  .execute(<b>{DATA}</b>)
 * @author hrishikeshprateek
 * An asynchronous task that analyzes call logs and generates statistics.
 */
public class CallLogsAnalyzer extends AsyncTask<String,Void, String> {

    private OnCallLogsAnalyzed onCallLogsAnalyzed;
    private @ArrangeBy int arrangeBy;
    private String queryPhoneNo;
    private @OperationModes Integer operationMode;
    private Map<String, GeneralLogOutput> callLogEntity;

    public static CallLogsAnalyzer getCallLogsAnalyzer(){
        return new CallLogsAnalyzer();
    }

    /**
     * @see OnCallLogsAnalyzed see the main OnCallLogsAnalyzed interface class docuentation for more info
     * @param onCallLogsAnalyzed instance of OnCallLogsAnalyzed Listener which returns <br>
     *     <b>void onExtractionSuccessFull(JSONObject data);</b><br>
     *     <b>void onFailedToAnalyze(AnalyzeException analyzeException);</b><br>
     *     <b>void onProgress(int processed, long total);</b><br>
     * @return Returns back the instance of the same initialized class
     */
    public CallLogsAnalyzer setOnCallLogsAnalyzedListener(OnCallLogsAnalyzed onCallLogsAnalyzed){
        this.onCallLogsAnalyzed = onCallLogsAnalyzed;
        return this;
    }

    /**
     * Sets the operation mode for call log analysis. it can be either <br>
     * <b>1. OperationModes.basicAnalyze</b><br>
     * <b>2. OperationModes.queryByNumber</b><br>
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
     * @param arrangeBy The arrangement mode to be set.
     * @return The instance of CallLogsAnalyzer.
     * @see ArrangeBy The ArrangeBy enum class click here.
     */
    public CallLogsAnalyzer setArrangeBy(@ArrangeBy int arrangeBy){
        this.arrangeBy = arrangeBy;
        return this;
    }
    /**
     * Sets the phone number to query for call log analysis.
     * @param phoneNo The phone number to be set for querying.
     * @return The instance of CallLogsAnalyzer.
     * @throws IllegalArgumentException If the operation mode is not set properly.
     */
    public CallLogsAnalyzer setQueryPhoneNo(String phoneNo){
        if (operationMode == null || operationMode == OperationModes.basicAnalyze){
            throw new IllegalArgumentException("Either operationMode() is not called or is not set to OperationModes.queryByNumber !");
        }
        this.queryPhoneNo = phoneNo;
        return this;
    }
    /**
     *
     * @see IllegalArgumentException click here to read more about IllegalArgumentException class
     * @throws IllegalArgumentException IllegalArgumentException tells weather all the parameters are set successfully or not before execution.
     * @param strings Takes the call logs json data as input.
     * @return Returns progress, error, analyzed data back.
     */
    @Override
    protected String doInBackground(String... strings) {
        String data = strings[0];

        if (onCallLogsAnalyzed == null || data.isEmpty())
            throw new IllegalArgumentException("onCallLogsAnalyzedListener() not set or data invalid !!");
        try {
            //updates call string JSON to Objects
            JSONArray jsonObject = new JSONArray(data);

            callLogEntity = new LinkedHashMap<>();

            for (int i = 0; i < jsonObject.length(); i++){
                onCallLogsAnalyzed.onProgress(i,jsonObject.length());
                JSONObject individualCallRecord = jsonObject.getJSONObject(i);
                if (individualCallRecord.has("NUMBER") && individualCallRecord.has("DURATION")) {
                    String phoneWithoutCountryCode = individualCallRecord.getString("NUMBER").replaceFirst("^\\+91","");
                    boolean isIncoming = !individualCallRecord.has("CALL_TYPE") || individualCallRecord.getString("CALL_TYPE").equalsIgnoreCase("INCOMING");

                    GeneralLogOutput output = callLogEntity.get(phoneWithoutCountryCode);

                    if (output != null) {
                        //Update entry
                        callLogEntity.replace(phoneWithoutCountryCode,
                                new GeneralLogOutput(output.getNUMBER(),
                                output.getCALL_COUNT()+1,
                                output.getDURATION() + Long.parseLong(individualCallRecord.getString("DURATION")),
                                output.getNAME(),
                                isIncoming ? (output.getINCOMING()+1) : output.getINCOMING(),
                                isIncoming ? output.getOUTGOING() : (output.getOUTGOING() + 1)));

                    }else {
                        //new Entry
                        callLogEntity.put(phoneWithoutCountryCode,
                                new GeneralLogOutput(phoneWithoutCountryCode,
                                        1,
                                        Long.parseLong(individualCallRecord.getString("DURATION")),
                                        individualCallRecord.getString("NAME"),
                                        isIncoming ? 1 : 0,
                                        isIncoming ? 0 : 1));
                    }
                }
            }

            long totalTalkTime = 0;
            String mostCalledNumber = null;
            long mostCalledValue = 0;

            JSONArray output = new JSONArray();
            for (Map.Entry<String, GeneralLogOutput> entry : callLogEntity.entrySet()) {
                GeneralLogOutput logOutput = entry.getValue();
                totalTalkTime += logOutput.getCALL_COUNT();

                long currentValue = (arrangeBy == ArrangeBy.duration) ?
                        logOutput.getDURATION() :
                        logOutput.getCALL_COUNT();

                if (currentValue > mostCalledValue) {
                    mostCalledNumber = logOutput.getNUMBER();
                    mostCalledValue = currentValue;
                }

                JSONObject jsonEntry = new JSONObject();
                jsonEntry.put(JSONConstants.NUMBER, entry.getKey());
                jsonEntry.put(JSONConstants.DURATION, logOutput.getDURATION());
                jsonEntry.put(JSONConstants.NAME, logOutput.getNAME());
                jsonEntry.put(JSONConstants.CALL_COUNT, logOutput.getCALL_COUNT());
                jsonEntry.put(JSONConstants.INCOMING_COUNT, logOutput.getINCOMING());
                jsonEntry.put(JSONConstants.OUTGOING_COUNT, logOutput.getOUTGOING());

                output.put(jsonEntry);
            }

            // Now sortedJsonArray contains the sorted log entries in JSON format
            JSONObject jsonObjectFinal = new JSONObject();
            jsonObjectFinal.put(JSONConstants.MOST_CONTACTED,mostCalledNumber);
            jsonObjectFinal.put(JSONConstants.TOTAL_TALK_TIME,totalTalkTime);
            jsonObjectFinal.put(JSONConstants.RECORDS,output);
            jsonObjectFinal.put(JSONConstants.QUERY_TYPE,arrangeBy == ArrangeBy.duration? "DURATION" : "COUNT");
            onCallLogsAnalyzed.onExtractionSuccessFull(jsonObjectFinal);

        }catch (Exception e){
            Log.e("ERR","h "+e.getMessage());
            e.printStackTrace();
            onCallLogsAnalyzed.onFailedToAnalyze(new AnalyzeException(e.getMessage(),e.getCause()));
        }
        return "com";
    }
}
