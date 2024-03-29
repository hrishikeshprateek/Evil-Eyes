package thundersharp.sensivisionhealth.loganalyzer.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TimeUtil {

    public static String convertSecondsToFormat(long seconds){
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        return (hours <= 9 ? "0" + hours : hours) + " hours, " +
                (minutes <= 9 ? "0" + minutes : minutes) + " min, " +
                (remainingSeconds <= 9 ? "0" + remainingSeconds : remainingSeconds) + " sec";
    }

    public static String convertFullDateToDDMMYYYY(String fullDate){
        try {
            // Define the input and output date format
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd:MM:yyyy", Locale.US);

            Date date = inputFormat.parse(fullDate);
            assert date != null;
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
