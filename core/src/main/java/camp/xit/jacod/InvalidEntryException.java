package camp.xit.jacod;

public class InvalidEntryException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public InvalidEntryException(String message) {
        super(message);
    }


    public InvalidEntryException(String message, Throwable cause) {
        super(message, cause);
    }


    public InvalidEntryException(Throwable cause) {
        super(cause);
    }
}
