package evil.eyes;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.hardware.Camera;
import android.os.Build;

public class MainActivityService extends Application {

    public static final String CHANNEL_ID = "ScheduleServiceChannel";
    public static final String CHANNEL_NAME = "Schedule Service Channel";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}