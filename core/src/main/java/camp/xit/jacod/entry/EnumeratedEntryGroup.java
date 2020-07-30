package camp.xit.jacod.entry;

import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumeratedEntryGroup<T extends CodelistEntry> implements EntryGroup<T> {

    private final Set<String> included;


    public EnumeratedEntryGroup(Set<String> included) {
        this.included = included;
    }


    @Override
    public Codelist<T> getEntries(Codelist<T> entries, boolean validOnly) {
        return entries.stream(validOnly)
                .filter(e -> included.contains(e.getCode()))
                .collect(Codelist.collect(entries.getName()));
    }


    public Set<String> getIncluded() {
        return included;
    }


    @Override
    public String toString() {
        return "EnumeratedEntryGroup{" + "included=" + included + '}';
    }


    @Override
    public List<String> getEntriesLazy(Codelist<T> entries, boolean validOnly) {
        return entries.stream(validOnly)
                .filter(e -> included.contains(e.getCode()))
                .map(item -> item.getCode())
                .collect(Collectors.toList());
    }
}
