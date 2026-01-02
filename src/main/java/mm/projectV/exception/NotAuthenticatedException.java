package mm.projectV.exception;
public class NotAuthenticatedException extends RuntimeException {

    public NotAuthenticatedException() {
        super("User is not authenticated");
    }

    public NotAuthenticatedException(String message) {
        super(message);
    }
}

