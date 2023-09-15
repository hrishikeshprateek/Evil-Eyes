package thundersharp.sensivisionhealth.loganalyzer.interfaces;

import org.json.JSONObject;

import thundersharp.sensivisionhealth.loganalyzer.core.errors.AnalyzeException;

public interface OnCallLogsAnalyzed {
    void onExtractionSuccessFull(JSONObject data);
    void onFailedToAnalyze(AnalyzeException analyzeException);
    void onProgress(int processed, long total);
}
