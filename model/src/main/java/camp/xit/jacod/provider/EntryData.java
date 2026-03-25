package camp.xit.jacod.provider;

import java.util.*;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import java.util.stream.Collectors;

public class EntryData extends HashMap<String, List<String>> {

    private static final long serialVersionUID = 1L;


    public EntryData() {
    }


    public EntryData(String field, String... values) {
        addField(field, values);
    }


    public void addField(String field, String... values) {
        addField(field, Arrays.asList(values));
    }


    public void addField(String field, Collection<String> values) {
        if (!isEmpty(values)) {
            put(field, new ArrayList<>(values));
        }
    }


    public List<String> getFieldValues(String field) {
        return get(field);
    }


    public List<String> getFieldValues(String field, boolean collection) {
        List<String> values = get(field);
        values = values == null ? Collections.emptyList() : values;
        if (collection && values != null && values.size() == 1) {
            String[] valuesArray = values.iterator().next().split(",");
            values = Arrays.stream(valuesArray).map(v -> v.trim()).collect(Collectors.toList());
        }
        return values;
    }


    /**
     * Returns first value or null if list of values is empty
     *
     * @param field name of field
     * @return first value or null if list of values is empty
     */
    public Optional<String> getSingleValue(String field) {
        List<String> values = get(field);
        return values == null || values.isEmpty() ? empty() : Optional.of(values.get(0));
    }


    private boolean isEmpty(Collection<String> values) {
        return values == null || (values.size() == 1 && values.iterator().next() == null);
    }


    public Optional<List<String>> getOpt(String field) {
        return ofNullable(get(field));
    }
}
