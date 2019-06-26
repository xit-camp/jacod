package camp.xit.jacoa.entry;

import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.CodelistEntry;

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


    public static <T extends CodelistEntry> Codelist<T> getEntries(EntryGroup<T> group, Codelist<T> entries, boolean validOnly) {
        return group != null ? group.getEntries(entries, validOnly) : entries.getEmpty();
    }
}
