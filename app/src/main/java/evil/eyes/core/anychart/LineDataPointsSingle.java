package evil.eyes.core.anychart;

import com.anychart.chart.common.dataentry.ValueDataEntry;

public class LineDataPointsSingle extends ValueDataEntry {

    public LineDataPointsSingle(String x, Number value, Number value2) {
        super(x, value);
        setValue("value2",value2);

    }

}