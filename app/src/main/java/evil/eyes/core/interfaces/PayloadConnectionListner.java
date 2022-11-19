package evil.eyes.core.interfaces;

public interface PayloadConnectionListner {
    void onConnectionSuccessful();
    void onPayloadError(String message);
}
