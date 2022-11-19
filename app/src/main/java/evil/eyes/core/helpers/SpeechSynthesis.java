package evil.eyes.core.helpers;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class SpeechSynthesis {

    public static SpeechSynthesis getInstance(Activity activity){
        return new SpeechSynthesis(activity);
    }

    private Activity activity;
    TextToSpeech t1 = null;

    public SpeechSynthesis(Activity activity) {
        this.activity = activity;
    }

    public void speak(String text){
        t1=new TextToSpeech(activity, status -> {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.US);
            }
        });
        t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);

    }
}
