package thundersharp.sensivisionhealth.loganalyzer.core;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import org.jetbrains.annotations.Contract;

import thundersharp.sensivisionhealth.loganalyzer.core.annos.ArrangeBy;
import thundersharp.sensivisionhealth.loganalyzer.core.annos.OperationModes;
import thundersharp.sensivisionhealth.loganalyzer.asyncs.AnalyzeCallLogsByPhone;
import thundersharp.sensivisionhealth.loganalyzer.asyncs.CallLogsAnalyzer;
import thundersharp.sensivisionhealth.loganalyzer.interfaces.OnCallLogsAnalyzed;

/**
 * @author hrishikeshprateek
 * An asynchronous task that analyzes call logs and generates statistics.
 * @apiNote Usage<br>
 * LogAnalyzerStarter
 * .getCallLogsAnalyzer()
 * .setArrangeBy(<b>{ArrangeBy}</b>)
 * .setOperationMode(<b>{OperationModes}</b>)
 * .setDataToProcess(<b>{DATA}</b>)
 * .analyze()
 */
public class LogAnalyzerStarter {

    private @ArrangeBy int arrangeBy;
    private @OperationModes Integer operationMode;
    private String queryPhoneNo;
    private String data;

    @NonNull
    @Contract(" -> new")
    public static LogAnalyzerStarter getCallLogsAnalyzer() {
        return new LogAnalyzerStarter();
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
    public LogAnalyzerStarter setOperationMode(@OperationModes int operationMode) {
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
    public LogAnalyzerStarter setArrangeBy(@ArrangeBy int arrangeBy) {
        this.arrangeBy = arrangeBy;
        return this;
    }

    /**
     * Sets the data to be processed usually is the json call log converted into String : <br>
     *
     * @param data The arrangement mode to be set.
     * @return The instance of CallLogsAnalyzer.
     */
    public LogAnalyzerStarter setDataToProcess(String data) {
        this.data = data;
        return this;
    }

    /**
     * Sets the phone number to query for call log analysis.
     *
     * @param phoneNo The phone number to be set for querying.
     * @return The instance of CallLogsAnalyzer.
     * @throws IllegalArgumentException If the operation mode is not set properly.
     */
    public LogAnalyzerStarter setQueryPhoneNo(String phoneNo) {
        if (operationMode == null || operationMode == OperationModes.basicAnalyze) {
            throw new IllegalArgumentException("Either operationMode() is not called or is not set to OperationModes.queryByNumber !");
        }
        this.queryPhoneNo = phoneNo;
        return this;
    }

    /**
     * @param onCallLogsAnalyzed instance of OnCallLogsAnalyzed Listener which returns <br>
     *                           <b>void onExtractionSuccessFull(JSONObject data);</b><br>
     *                           <b>void onFailedToAnalyze(AnalyzeException analyzeException);</b><br>
     *                           <b>void onProgress(int processed, long total);</b><br>
     * @apiNote  Returns back the instance of the same initialized class
     * @see OnCallLogsAnalyzed see the main OnCallLogsAnalyzed interface class docuentation for more info
     * @noinspection deprecation
     */
    public void analyze(OnCallLogsAnalyzed onCallLogsAnalyzed) {
        if (onCallLogsAnalyzed == null)
            throw new IllegalArgumentException("Listener not attached !!");
        if (operationMode != null && operationMode == OperationModes.queryByNumber) {
            //Fire up the Query by number logic Api
            Log.e("LEVEL","Async fired !!");
            AnalyzeCallLogsByPhone.getCallLogsAnalyzerByPhone(onCallLogsAnalyzed,queryPhoneNo)
                    .execute(data);
        } else {
            //Fire up the basic analyze api
            CallLogsAnalyzer.getCallLogsAnalyzer(onCallLogsAnalyzed, arrangeBy)
                    .execute(data);
        }
    }

    public void releaseMemory(Context context){
        Toast.makeText(context, "Successfully cleaned up the memory", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
