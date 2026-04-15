package camp.xit.jacod.entry;

import java.util.stream.Stream;

import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;

public class EmptyEntryGroup<T extends CodelistEntry> implements EntryGroup<T> {

    @Override
    public Codelist<T> getEntries(Codelist<T> entries, boolean validOnly) {
        return Stream.<T>empty().collect(Codelist.collect(entries.getName()));
    }
}
