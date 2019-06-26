package camp.xit.jacoa.impl;

import static camp.xit.jacoa.impl.EntryMetadata.getReferenceType;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class ValueParser {

    private ValueParser() {
    }


    static Integer parseInt(String strValue) {
        Integer result;
        strValue = strValue.trim();
        try {
            result = Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            result = Double.valueOf(Double.parseDouble(strValue)).intValue();
        }
        return result;
    }


    static Long parseLong(String strValue) {
        Long result;
        strValue = strValue.trim();
        try {
            result = Long.parseLong(strValue);
        } catch (NumberFormatException e) {
            result = Double.valueOf(Double.parseDouble(strValue)).longValue();
        }
        return result;
    }


    static Byte parseByte(String strValue) {
        Byte result;
        strValue = strValue.trim();
        try {
            result = Byte.parseByte(strValue);
        } catch (NumberFormatException e) {
            result = Double.valueOf(Double.parseDouble(strValue)).byteValue();
        }
        return result;
    }


    static Double parseDouble(String strValue) {
        return Double.parseDouble(strValue.trim());
    }


    static Float parseFloat(String strValue) {
        return Float.parseFloat(strValue.trim());
    }


    static LocalDate parseDate(String strValue) {
        LocalDate result;
        strValue = strValue.trim();
        if (strValue.length() > 10) {
            result = LocalDate.parse(strValue, DateTimeFormatter.ISO_DATE_TIME);
        } else {
            result = LocalDate.parse(strValue, DateTimeFormatter.ISO_DATE);
        }
        return result;
    }


    static LocalDateTime parseDateTime(String strValue) {
        return LocalDateTime.parse(strValue.trim(), DateTimeFormatter.ISO_DATE_TIME);
    }


    static BigDecimal parseDecimal(String strValue) {
        return new BigDecimal(strValue.trim());
    }


    static Boolean parseBoolean(String strValue) {
        return Boolean.parseBoolean(strValue.trim());
    }


    static Object parseSimpleValue(String strValue, Class<?> type) {
        Object result = null;
        if (String.class.isAssignableFrom(type)) {
            result = strValue;
        } else if (Boolean.class.isAssignableFrom(type)) {
            result = parseBoolean(strValue);
        } else if (Byte.class.isAssignableFrom(type)) {
            result = parseByte(strValue);
        } else if (Integer.class.isAssignableFrom(type)) {
            result = parseInt(strValue);
        } else if (Long.class.isAssignableFrom(type)) {
            result = parseLong(strValue);
        } else if (Float.class.isAssignableFrom(type)) {
            result = parseFloat(strValue);
        } else if (Double.class.isAssignableFrom(type)) {
            result = parseDouble(strValue);
        } else if (BigDecimal.class.isAssignableFrom(type)) {
            result = parseDecimal(strValue);
        } else if (LocalDate.class.isAssignableFrom(type)) {
            result = parseDate(strValue);
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            result = parseDateTime(strValue);
        }
        return result;
    }


    static List<Object> parseCollectionOfSimple(Collection<String> strValues, Class<?> entryType) {
        return strValues.stream().map(str -> parseSimpleValue(str, entryType)).collect(Collectors.toList());
    }


    static boolean isSimpleType(Field field) {
        return isSimpleType(getReferenceType(field));
    }


    static boolean isSimpleType(Class<?> type) {
        return String.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type)
                || BigDecimal.class.isAssignableFrom(type)
                || LocalDate.class.isAssignableFrom(type)
                || LocalDateTime.class.isAssignableFrom(type);
    }
}
