package thundersharp.sensivisionhealth.loganalyzer.models;

public class QueryByPhoneRecord {
    private String CALL_DATE;
    private long DURATION,INCOMING,OUTGOING,MISSED,CALL_COUNT;

    public QueryByPhoneRecord(String CALL_DATE, long CALL_COUNT, long DURATION, long INCOMING, long OUTGOING, long MISSED) {
        this.CALL_DATE = CALL_DATE;
        this.CALL_COUNT = CALL_COUNT;
        this.DURATION = DURATION;
        this.INCOMING = INCOMING;
        this.OUTGOING = OUTGOING;
        this.MISSED = MISSED;
    }

    public String getCALL_DATE() {
        return CALL_DATE;
    }

    public void setCALL_DATE(String CALL_DATE) {
        this.CALL_DATE = CALL_DATE;
    }

    public long getCALL_COUNT() {
        return CALL_COUNT;
    }

    public void setCALL_COUNT(long CALL_COUNT) {
        this.CALL_COUNT = CALL_COUNT;
    }

    public long getDURATION() {
        return DURATION;
    }

    public void setDURATION(long DURATION) {
        this.DURATION = DURATION;
    }

    public long getINCOMING() {
        return INCOMING;
    }

    public void setINCOMING(long INCOMING) {
        this.INCOMING = INCOMING;
    }

    public long getOUTGOING() {
        return OUTGOING;
    }

    public void setOUTGOING(long OUTGOING) {
        this.OUTGOING = OUTGOING;
    }

    public long getMISSED() {
        return MISSED;
    }

    public void setMISSED(long MISSED) {
        this.MISSED = MISSED;
    }
}
