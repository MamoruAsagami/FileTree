package sophie.ui;

public class CancelledByTheUserException extends java.lang.Exception {

    // This one signales that the user cancelled the processing in between.
    // So, no new error message is needed.

    public CancelledByTheUserException() {
    }

    public CancelledByTheUserException(String message) {
        super(message);
    }
}
