package camp.xit.jacod.model;

import camp.xit.jacod.EntryNotFoundException;
import java.util.Collection;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import static java.util.Optional.ofNullable;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Codelist<V extends CodelistEntry> extends HashMap<String, V> implements Iterable<V> {

    private static final long serialVersionUID = 1L;

    private final String name;


    public Codelist(String name) {
        this.name = name;
    }


    public Codelist(String name, Collection<V> entries) {
        super(entries.stream().collect(Collectors.toMap(V::getCode, e -> e)));
        this.name = name;
    }


    /**
     * Return all entries. To return only valid values, use {@link #values(boolean)}
     *
     * @return all entries
     */
    @Override
    public Iterator<V> iterator() {
        return values(false).iterator();
    }


    public Collection<V> values(boolean validOnly) {
        return validOnly ? validValues() : values();
    }


    private Collection<V> validValues() {
        return stream(true).collect(Collectors.toList());
    }


    /**
     * Returns stream of all entries. To return stream of valid entries, use {@link #stream(boolean)}
     *
     * @return stream
     */
    public Stream<V> stream() {
        return stream(false);
    }


    public Stream<V> stream(boolean validOnly) {
        Stream<V> stream = values().stream();
        return validOnly ? stream.filter(v -> v.isValid()) : stream;
    }


    /**
     * Returns parallel stream of all entries. To return stream of valid entries, use
     * {@link #parallelStream(boolean)}
     *
     * @return parallel stream
     */
    public Stream<V> parallelStream() {
        return parallelStream(false);
    }


    public Stream<V> parallelStream(boolean validOnly) {
        Stream<V> stream = values().parallelStream();
        return validOnly ? stream.filter(v -> v.isValid()) : stream;
    }


    public V getEntry(String code) {
        return ofNullable(get(code)).orElseThrow(() -> new EntryNotFoundException(name, code));
    }


    public V getEntry(CodelistEnum<V> code) {
        return getEntry(code.toString());
    }


    public boolean hasEntry(String code) {
        return containsKey(code);
    }


    public V add(V e) {
        return put(e.getCode(), e);
    }


    public void addAll(Collection<? extends V> c) {
        c.forEach(e -> add(e));
    }


    public void addAll(Codelist<? extends V> c) {
        c.forEach(e -> add(e));
    }


    /**
     * Return ordered list of all entries. To return list of valid values, use
     * {@link #getOrderedList(boolean)}
     *
     * @return ordered list of entries.
     */
    public List<V> getOrderedList() {
        return getOrderedList(false);
    }


    public List<V> getOrderedList(boolean validOnly) {
        return stream(validOnly)
                .sorted(nullsLast(comparing(CodelistEntry::getOrder, nullsLast(naturalOrder()))))
                .collect(Collectors.toList());
    }


    public final static <T extends CodelistEntry> Collector<T, ?, Codelist<T>> collect(Class<T> clazz) {
        return collect(clazz.getSimpleName());
    }


    public final static <T extends CodelistEntry> Collector<T, ?, Codelist<T>> collect(String name) {
        return Collector.of((Supplier<Codelist<T>>) () -> new Codelist<T>(name), Codelist::add, (l, r) -> {
            l.addAll(r);
            return l;
        });
    }


    public String getName() {
        return name;
    }


    public Codelist<V> getEmpty() {
        return new Codelist(name);
    }
}
