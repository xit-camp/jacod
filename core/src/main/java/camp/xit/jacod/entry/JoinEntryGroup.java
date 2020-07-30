package camp.xit.jacod.entry;

import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JoinEntryGroup<T extends CodelistEntry> implements EntryGroup<T> {

    private final List<EntryGroup<T>> entryGroups;


    public JoinEntryGroup(List<EntryGroup<T>> entryGroups) {
        this.entryGroups = entryGroups;
    }


    public JoinEntryGroup(List<? extends EntryGroup<T>> groups, EnumeratedEntryGroup<T> enumerated) {
        this.entryGroups = new ArrayList<>(groups);
        this.entryGroups.add(enumerated);
    }


    @Override
    public Codelist<T> getEntries(Codelist<T> entries, boolean validOnly) {
        return entryGroups.parallelStream()
                .flatMap(e -> e.getEntries(entries).stream(validOnly))
                .collect(Codelist.collect(entries.getName()));
    }
    
    public List<String> getEntriesLazy(Codelist<T> entries, boolean validOnly) {
        return entryGroups.parallelStream()
                .map(item -> {
                    if (item instanceof QueryEntryGroup) {
                        return Arrays.asList(((QueryEntryGroup)item).getObjectId());
                    }
                    return item.getEntriesLazy(entries, validOnly);
                })
        .flatMap(List::stream)
        .collect(Collectors.toList());
    }


    @Override
    public String toString() {
        return "JoinEntryGroup{" + String.valueOf(entryGroups) + '}';
    }
}
