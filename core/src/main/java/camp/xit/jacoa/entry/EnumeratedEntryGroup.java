package camp.xit.jacoa.entry;

import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.CodelistEntry;
import java.util.Set;

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
}
