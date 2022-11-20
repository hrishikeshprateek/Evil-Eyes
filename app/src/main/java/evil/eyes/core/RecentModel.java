package evil.eyes.core;

public class RecentModel {
    public RecentModel(){}
    public String link,name,timeStamp,key;

    public RecentModel(String link, String name, String timeStamp, String key) {
        this.link = link;
        this.name = name;
        this.timeStamp = timeStamp;
        this.key = key;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }
}
