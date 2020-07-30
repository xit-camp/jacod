package camp.xit.jacod.entry;

import java.util.List;

import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;

public interface EntryGroup<T extends CodelistEntry> {

    /**
     * Vráti validné filtrované entries
     *
     * @param entries
     * @return
     */
    default Codelist<T> getEntries(Codelist<T> entries) {
        return getEntries(entries, true);
    }


    /**
     * Vráti filtrované entries
     *
     * @param entries
     * @return
     */
    Codelist<T> getEntries(Codelist<T> entries, boolean validOnly);
    
    /**
     * Vráti zoznam ID (bp kód alebo kód skupiny) entries 
     * 
     * @param entries
     * @param validOnly
     * @return
     */
    List<String> getEntriesLazy(Codelist<T> entries, boolean validOnly);


    public static <T extends CodelistEntry> Codelist<T> getEntries(EntryGroup<T> group, Codelist<T> entries, boolean validOnly) {
        return group != null ? group.getEntries(entries, validOnly) : entries.getEmpty();
    }
}
