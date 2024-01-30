package evil.eyes.core.models;

public class Devices {

    private String uuid;
    private String lastStartedTime;
    private String primaryAccountName;

    public Devices() {
    }



    public Devices(String UUID, String lastStartedTime, String primaryAccountName) {
        this.uuid = UUID;
        this.lastStartedTime = lastStartedTime;
        this.primaryAccountName = primaryAccountName;
    }

    public String getUUID() {
        return uuid;
    }

    public String getLastStartedTime() {
        return lastStartedTime;
    }

    public String getPrimaryAccountName() {
        return primaryAccountName;
    }
}
