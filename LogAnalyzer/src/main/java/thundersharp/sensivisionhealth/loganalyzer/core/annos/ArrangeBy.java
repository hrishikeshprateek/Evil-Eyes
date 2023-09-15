package thundersharp.sensivisionhealth.loganalyzer.core.annos;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@IntDef({ArrangeBy.count,ArrangeBy.duration})
public @interface ArrangeBy {
    int count = 0;
    int duration = 1;
}
