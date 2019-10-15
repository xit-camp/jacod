package camp.xit.kiwi.jacod.provider.gsheet.service;

public class NotFoundException extends GoogleApiException {

    public NotFoundException(String message) {
        super(message, 404);
    }


    public NotFoundException(String message, Throwable cause) {
        super(message, 404, cause);
    }
}
