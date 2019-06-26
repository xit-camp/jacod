package camp.xit.jacoa.provider;

public class CodelistNotChangedException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public CodelistNotChangedException(String key) {
        super("Entry " + key + " did not change");
    }


    public CodelistNotChangedException(String key, Throwable cause) {
        super("Entry " + key + " did not change", cause);
    }
}
