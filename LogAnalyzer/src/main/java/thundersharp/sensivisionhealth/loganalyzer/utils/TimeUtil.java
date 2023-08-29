package thundersharp.sensivisionhealth.loganalyzer.utils;

public class TimeUtil {

    public static String convertSecondsToFormat(long seconds){
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        return hours + " hours, " + minutes + " minutes, " + remainingSeconds + " seconds";
    }
}
