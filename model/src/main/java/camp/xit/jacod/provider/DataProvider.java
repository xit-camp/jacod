package camp.xit.jacod.provider;

import camp.xit.jacod.model.CodelistEntry;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DataProvider {

    /**
     * Načíta dáta entry z dátového zdroja. Parameter lastRead hovorí čas, kedy naposledy bola táto entry
     * čítaná. Ak entry nebola v časovom intervale [lastRead, now] zmenená, tak DataProvider môže vyhodiť
     * výnimku * {@link EntryNotChangedException}.
     *
     * @param codelist názov číselníka
     * @param lastReadTime čas posledného predošlého čítania (Používa sa pri kešovaní hodnôt)
     * @return
     */
    Optional<List<EntryData>> readEntries(String codelist, long lastReadTime);


    /**
     * Vráti zoznam všetkých možných číselníkov. Táto metóda je nepovinná. Ak dátový zdroj nepodporuje
     * získavanie zoznamu, tak vráti prázdnu množinu.
     *
     *
     * @return zoznam všetkých číselníkov
     */
    default Set<String> getCodelistNames() {
        return Collections.emptySet();
    }


    /**
     * Trieda implementácie {@link DataProvider}, ktorá sa použije pri mapovaní na objekty
     * {@link CodelistEntry}
     *
     * @return trieda implementácie {@link DataProvider}
     */
    default Class<? extends DataProvider> getProviderClass() {
        return this.getClass();
    }


    /**
     * Get DataProvider name used e.g. in logs
     *
     * @return name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
