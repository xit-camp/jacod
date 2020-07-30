package camp.xit.jacod.entry;

import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class EmptyEntryGroup<T extends CodelistEntry> implements EntryGroup<T> {

    @Override
    public Codelist<T> getEntries(Codelist<T> entries, boolean validOnly) {
        return Stream.<T>empty().collect(Codelist.collect(entries.getName()));
    }

    @Override
    public List<String> getEntriesLazy(Codelist<T> entries, boolean validOnly) {
        return new ArrayList<String>();
    }
}
