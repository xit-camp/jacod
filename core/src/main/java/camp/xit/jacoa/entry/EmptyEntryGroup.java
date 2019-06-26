package camp.xit.jacoa.entry;

import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.CodelistEntry;
import java.util.stream.Stream;

public class EmptyEntryGroup<T extends CodelistEntry> implements EntryGroup<T> {

    @Override
    public Codelist<T> getEntries(Codelist<T> entries, boolean validOnly) {
        return Stream.<T>empty().collect(Codelist.collect(entries.getName()));
    }
}
