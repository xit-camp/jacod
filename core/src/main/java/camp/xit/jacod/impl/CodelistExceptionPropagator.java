package camp.xit.jacod.impl;

import camp.xit.jacod.CodelistNotFoundException;
import camp.xit.jacod.EntryNotFoundException;
import java.sql.Timestamp;
import org.cache2k.expiry.ExpiryTimeValues;
import org.cache2k.integration.CacheLoaderException;
import org.cache2k.integration.ExceptionInformation;
import org.cache2k.integration.ExceptionPropagator;

public class CodelistExceptionPropagator implements ExceptionPropagator {

    @Override
    public RuntimeException propagateException(Object key, final ExceptionInformation exceptionInformation) {
        Throwable origException = exceptionInformation.getException();
        return (origException instanceof CodelistNotFoundException
                || origException instanceof EntryNotFoundException)
                        ? (RuntimeException) origException
                        : origPropagateException(key, exceptionInformation);
    }


    private RuntimeException origPropagateException(Object key, final ExceptionInformation exceptionInformation) {
        long _expiry = exceptionInformation.getUntil();
        String txt = "";
        if (_expiry > 0) {
            if (_expiry == ExpiryTimeValues.ETERNAL) {
                txt = "expiry=ETERNAL, cause: ";
            } else {
                txt = "expiry=" + formatMillis(_expiry) + ", cause: ";
            }
        }
        return new CacheLoaderException(txt + exceptionInformation.getException(), exceptionInformation.getException());
    }


    /**
     * Use the SQL timestamp for a compact time output. The time is formatted in the default timezone.
     */
    private String formatMillis(long t) {
        return new Timestamp(t).toString();
    }

}
