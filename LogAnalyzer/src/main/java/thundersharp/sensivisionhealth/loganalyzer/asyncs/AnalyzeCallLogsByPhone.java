package thundersharp.sensivisionhealth.loganalyzer.asyncs;

import static thundersharp.sensivisionhealth.loganalyzer.core.helpers.CallLogsUtil.isIncoming;
import static thundersharp.sensivisionhealth.loganalyzer.core.helpers.CallLogsUtil.isMissed;
import static thundersharp.sensivisionhealth.loganalyzer.core.helpers.CallLogsUtil.isOutgoing;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import thundersharp.sensivisionhealth.loganalyzer.constants.JSONConstants;
import thundersharp.sensivisionhealth.loganalyzer.core.helpers.CallLogsUtil;
import thundersharp.sensivisionhealth.loganalyzer.core.errors.AnalyzeException;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;
import thundersharp.sensivisionhealth.loganalyzer.models.QueryByPhoneRecord;
import thundersharp.sensivisionhealth.loganalyzer.utils.TimeUtil;

public class AnalyzeCallLogsByPhone extends AsyncTask<String,Void,Void> {
    private final OnCallLogsAnalyzed onCallLogsAnalyzed;
    private final String queryPhoneNumber;
    private String queryNumber = "";
    private String nameSaved = "";
    private long incomingCount = 0;
    private long outgoingCount = 0;
    private long missedCount = 0;
    private int totalRecordDays = 0;
    private int totalDuration = 0;


    public AnalyzeCallLogsByPhone(OnCallLogsAnalyzed onCallLogsAnalyzed, String queryPhoneNumber) {
        this.onCallLogsAnalyzed = onCallLogsAnalyzed;
        this.queryPhoneNumber = queryPhoneNumber;
    }

    public static AnalyzeCallLogsByPhone getCallLogsAnalyzerByPhone(OnCallLogsAnalyzed onCallLogsAnalyzed, String queryPhoneNumber) {
        return new AnalyzeCallLogsByPhone(onCallLogsAnalyzed, queryPhoneNumber);
    }

    @Override
    protected Void doInBackground(String... strings) {
        String data = strings[0];
        if (onCallLogsAnalyzed == null || data == null || data.isEmpty())
            throw new IllegalArgumentException("Call logs transfer failed or onCallLogsAnalyzed Listener not attached !!");
        try{
            JSONObject parsedLogs = parseLogsSpecificToPhone(data, CallLogsUtil.removeContryCode(queryPhoneNumber));
            onCallLogsAnalyzed.onExtractionSuccessFull(parsedLogs);
            Log.e("DATA",parsedLogs.toString());

        }catch (Exception e){
            onCallLogsAnalyzed.onFailedToAnalyze(new AnalyzeException(e.getMessage(),e.getCause()));
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject parseLogsSpecificToPhone(String callLogData, String queryPhoneNumber) throws JSONException {
        Log.e("LEVEL","Async working !!");
        Map<String, QueryByPhoneRecord> callLogEntity = new LinkedHashMap<>();
        JSONArray data = new JSONArray(callLogData);
        int counterProgress = 0;
        for (int i=0; i < data.length(); i++){

            JSONObject jsonObject = data.getJSONObject(i);
            if (CallLogsUtil.removeContryCode(jsonObject.getString(JSONConstants.NUMBER)).equalsIgnoreCase(CallLogsUtil.removeContryCode(queryPhoneNumber))){
                //Updating global indexes of values
                onCallLogsAnalyzed.onProgress(++counterProgress, data.length());
                queryNumber = CallLogsUtil.removeContryCode(jsonObject.getString(JSONConstants.NUMBER));
                nameSaved = jsonObject.has(JSONConstants.NAME)  ?  jsonObject.getString(JSONConstants.NAME) : "Name N/A";
                String call_type = "UNSPECIFIED";
                if (jsonObject.has(JSONConstants.CALL_TYPE))
                    call_type = jsonObject.getString(JSONConstants.CALL_TYPE);
                //Log.e(String.valueOf(i),call_type);
                totalDuration += Integer.parseInt(jsonObject.getString(JSONConstants.DURATION));

                if (!call_type.isEmpty()){
                    //Updating call type count of the specific number
                    if (call_type.equalsIgnoreCase(JSONConstants.INCOMING_COUNT)) incomingCount++;
                    else if (call_type.equalsIgnoreCase(JSONConstants.OUTGOING_COUNT)) outgoingCount++;
                    else if (call_type.equalsIgnoreCase(JSONConstants.MISSED_COUNT)) missedCount++;
                }

                String callDate = TimeUtil.convertFullDateToDDMMYYYY(jsonObject.getString(JSONConstants.CALL_DATE));
                QueryByPhoneRecord existing = callLogEntity.get(callDate);
                boolean isIncoming = isIncoming(jsonObject);
                boolean isOutgoing = isOutgoing(jsonObject);
                boolean isMissed = isMissed(jsonObject);
                //Check for dates
                if (existing != null){
                    //Update here
                    callLogEntity
                            .replace(callDate,new QueryByPhoneRecord(
                                    callDate,
                                    existing.getCALL_COUNT() +1,
                                    existing.getDURATION() + Long.parseLong(jsonObject.getString(JSONConstants.DURATION)),
                                    isIncoming ? existing.getINCOMING() + 1 : existing.getINCOMING(),
                                    isOutgoing ? existing.getOUTGOING() + 1 : existing.getOUTGOING(),
                                    isMissed ? existing.getMISSED() + 1 : existing.getMISSED()
                            ));
                }else {
                    //Create a new entry
                    totalRecordDays++;
                    callLogEntity.put(callDate,new QueryByPhoneRecord(callDate,
                            1,
                            Long.parseLong(jsonObject.getString(JSONConstants.DURATION)),
                            isIncoming ? 1 : 0,
                            isOutgoing ? 1 : 0,
                            isMissed ? 1 : 0));
                }

            }
        }

        JSONObject output = new JSONObject();
        output.put(JSONConstants.QUERY_NUMBER,queryNumber);
        output.put(JSONConstants.NAME_SAVED,nameSaved);
        output.put(JSONConstants.TOTAL_TALK_TIME_OUT, totalDuration);
        output.put(JSONConstants.OUTGOING_COUNT_OUT,outgoingCount);
        output.put(JSONConstants.INCOMING_COUNT_OUT, incomingCount);
        output.put(JSONConstants.MISSED_COUNT_OUT, missedCount);
        output.put(JSONConstants.TOTAL_RECORD_DAYS,totalRecordDays);
        JsonArray data_daily = new Gson().toJsonTree(callLogEntity.values()).getAsJsonArray();
        //Log.e("GOOGLE", data_daily.toString());
        output.put(JSONConstants.DAILY_RECORDS, new JSONArray(data_daily.toString()));
        return output;
    }
}
