package thundersharp.sensivisionhealth.loganalyzer.core.helpers;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class CallLogsUtil {
    public static String removeContryCode(String number) {
        return number.replaceFirst("^\\+91", "");
    }

    public static boolean isIncoming(JSONObject individualCallRecord) throws JSONException {
        return !individualCallRecord.has("CALL_TYPE") || individualCallRecord.getString("CALL_TYPE").equalsIgnoreCase("INCOMING");
    }

    public static boolean isOutgoing(JSONObject individualCallRecord) throws JSONException {
        return !individualCallRecord.has("CALL_TYPE") || individualCallRecord.getString("CALL_TYPE").equalsIgnoreCase("OUTGOING");
    }

    public static boolean isMissed(JSONObject individualCallRecord) throws JSONException {
        return !individualCallRecord.has("CALL_TYPE") || individualCallRecord.getString("CALL_TYPE").equalsIgnoreCase("MISSED");
    }
}
