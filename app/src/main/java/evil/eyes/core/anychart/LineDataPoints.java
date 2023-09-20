package evil.eyes.core.anychart;

import com.anychart.chart.common.dataentry.ValueDataEntry;

public class LineDataPoints extends ValueDataEntry {

    LineDataPoints(String x, Number value, Number value2, Number value3) {
        super(x, value);
        setValue("value2", value2);
        setValue("value3", value3);
    }

}