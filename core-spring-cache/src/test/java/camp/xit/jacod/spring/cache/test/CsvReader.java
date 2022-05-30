package camp.xit.jacod.spring.cache.test;

import java.util.ArrayList;
import java.util.List;

final class CsvReader {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DOUBLE_QUOTES = '"';
    private static final char DEFAULT_QUOTE_CHAR = DOUBLE_QUOTES;

    private CsvReader() {}

    public static List<String> parseLine(String line) {
        return parseLine(line, DEFAULT_SEPARATOR);
    }

    public static List<String> parseLine(String line, char separator) {
        return parse(line, separator, DEFAULT_QUOTE_CHAR);
    }

    private static List<String> parse(String line, char separator, char quoteChar) {

        List<String> result = new ArrayList<>();

        boolean inQuotes = false;
        boolean isFieldWithEmbeddedDoubleQuotes = false;

        StringBuilder field = new StringBuilder();

        for (char c : line.toCharArray()) {

            if (c == DOUBLE_QUOTES) { // handle embedded double quotes ""
                if (isFieldWithEmbeddedDoubleQuotes) {

                    if (field.length() > 0) { // handle for empty field like "",""
                        field.append(DOUBLE_QUOTES);
                        isFieldWithEmbeddedDoubleQuotes = false;
                    }

                } else {
                    isFieldWithEmbeddedDoubleQuotes = true;
                }
            } else {
                isFieldWithEmbeddedDoubleQuotes = false;
            }

            if (c == quoteChar) {
                inQuotes = !inQuotes;
            } else {
                if (c == separator && !inQuotes) { // if find separator and not in quotes, add field to the list
                    result.add(field.toString());
                    field.setLength(0); // empty the field and ready for the next
                } else {
                    field.append(c); // else append the char into a field
                }
            }
        }

        // line done, what to do next?
        result.add(field.toString()); // this is the last field

        return result;
    }
}
