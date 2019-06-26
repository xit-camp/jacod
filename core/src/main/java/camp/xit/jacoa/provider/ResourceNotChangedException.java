package camp.xit.jacoa.provider;

import java.io.IOException;

public class ResourceNotChangedException extends IOException {

    public ResourceNotChangedException(String resource) {
        super("Entry " + resource + " did not change");
    }


    public ResourceNotChangedException(String resource, Throwable cause) {
        super("Entry " + resource + " did not change", cause);
    }
}
