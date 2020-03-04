package camp.xit.jacod.spring.cache;

import java.time.Duration;

class Tuple<T> {

    final T value;
    long lastModified;


    public Tuple(T value) {
        this.value = value;
        this.lastModified = System.currentTimeMillis();
    }


    public Tuple(T value, long lastModified) {
        this.value = value;
        this.lastModified = lastModified;
    }


    public boolean isValid(Duration expiryTime) {
        return System.currentTimeMillis() - lastModified < expiryTime.toMillis();
    }


    public boolean isInvalid(Duration expiryTime) {
        return !isValid(expiryTime);
    }
}
