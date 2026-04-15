package camp.xit.jacod.provider;

import java.io.IOException;

public class ResourceNotFoundException extends IOException {

    public ResourceNotFoundException(String resource) {
        super("Resource " + resource + " not found");
    }


    public ResourceNotFoundException(String resource, Throwable cause) {
        super("Resource " + resource + " not found", cause);
    }
}
