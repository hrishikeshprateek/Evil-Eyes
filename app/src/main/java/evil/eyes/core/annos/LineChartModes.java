package evil.eyes.core.annos;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@IntDef({LineChartModes.MODE_DEFAULT,
LineChartModes.MODE_INCOMING,
LineChartModes.MODE_OUTGOING,
LineChartModes.MODE_MISSED})
public @interface LineChartModes {
    int MODE_DEFAULT = 0;
    int MODE_INCOMING = 1;
    int MODE_OUTGOING = 2;
    int MODE_MISSED = 3;
}
