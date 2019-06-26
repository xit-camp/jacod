package camp.xit.jacod;

import java.time.LocalDate;

public final class DateUtil {

    private DateUtil() {
    }


    public static final boolean isValid(LocalDate validFrom, LocalDate validTo) {
        return isValid(validFrom, validTo, LocalDate.now());
    }


    public static final boolean isValid(LocalDate validFrom, LocalDate validTo, LocalDate refDate) {
        validTo = validTo == null ? LocalDate.MAX : validTo;
        return refDate != null
                && (validFrom == null || refDate.isEqual(validFrom) || refDate.isAfter(validFrom))
                && (validTo == null || refDate.isEqual(validTo) || refDate.isBefore(validTo));
    }
}
