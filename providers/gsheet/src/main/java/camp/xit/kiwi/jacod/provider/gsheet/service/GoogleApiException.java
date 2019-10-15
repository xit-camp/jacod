package camp.xit.kiwi.jacod.provider.gsheet.service;

public class GoogleApiException extends RuntimeException {

    private final int status;


    public GoogleApiException(String message, int status) {
        super(message);
        this.status = status;
    }


    public GoogleApiException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }


    public int getStatus() {
        return status;
    }
}
