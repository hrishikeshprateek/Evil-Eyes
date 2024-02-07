package evil.eyes.core;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.ref.WeakReference;

import evil.eyes.core.models.Devices;

/**
 *
 */
public class DeviceConfig {

    private static DeviceConfig deviceConfig = null;
    private final WeakReference<Context> contextWeakReference;
    private SharedPreferences sharedPreferences;

    public static DeviceConfig getInstance(Context context){
        if (deviceConfig == null) deviceConfig = new DeviceConfig(context);
        return deviceConfig;
    }

    private DeviceConfig(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    public DeviceConfig initializeStorage(){
        sharedPreferences = contextWeakReference.get().getSharedPreferences("DEVICE_STATE",Context.MODE_PRIVATE);
        return this;
    }

    public DeviceConfig setCurrentDevice(Devices device){
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.putString("UUID",device.getUUID());
            editor.putString("NAME",device.getPrimaryAccountName());
            editor.putString("ACTIVE",device.getLastStartedTime());
            editor.apply();
        }
        return this;
    }

    public Devices getSelectedDevice() {
        if (sharedPreferences != null)
            return new Devices(sharedPreferences.getString("UUID",null),
                    sharedPreferences.getString("ACTIVE",null),
                    sharedPreferences.getString("NAME",null));
        else return null;
    }
}
