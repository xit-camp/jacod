package camp.xit.jacoa;

import camp.xit.jacoa.entry.parser.ast.CompileException;
import camp.xit.jacoa.impl.CachedCodelistClientImpl;
import camp.xit.jacoa.impl.CodelistClientImpl;
import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.CodelistEntry;
import camp.xit.jacoa.model.CodelistEnum;
import camp.xit.jacoa.provider.DataProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface CodelistClient {

    /**
     * Vráti všetky platné čiselníkové hodnoty daného číselníka. Číselník je jednoznačne identifikovaný
     * názvom. V názve číselníka nezáleží na veľkosti písmen a na delenie slov v názve je možné použiť znak
     * '_' alebo '-' t.j. napríklad názov DOCUMENT_TYPE je ekvivalentný s document-type. Káždá hodnota
     * obsahuje všetky atribúty. Ak chceš definovať zoznam vrátených atribútov, tak použi
     * {@link #getCodelist(java.lang.String, java.lang.String[])}. Ak číselník z daným menom neexistuje, tak
     * sa vyhodí výnimka {@link CodelistNotFoundException}.
     *
     * Táto metóda vracia iba číselníky z hlavnej cache a preto nevracia číselníky definované používateľom.
     * To znamená, že ak používaš svoje odvodené triedy číselníkov, tak použí metódu
     * {@link #getCodelist(java.lang.Class)}.
     *
     * @param codelist názov číselníka
     * @throws CodelistNotFoundException ak číselník neexistuje
     * @return zoznam všetkých čiselníkových hodnôt
     */
    Codelist<? extends CodelistEntry> getCodelist(String codelist);


    /**
     * Vráti všetky platné čiselníkové hodnoty daného číselníka. Ak číselník z daným menom neexistuje, tak sa
     * vyhodí výnimka {@link CodelistNotFoundException}. Táto operácia vráti všetky možné číselníky, vrátane
     * použivateľsky definovaných číselníkov.
     *
     * @param <T> typ číselníkka
     * @param entryClass trieda číselníka
     * @throws CodelistNotFoundException ak číselník neexistuje
     * @return zoznam všetkých čiselníkových hodnôt
     */
    <T extends CodelistEntry> Codelist<T> getCodelist(Class<T> entryClass);


    /**
     * Vráti všetky platné čiselníkové hodnoty daného číselníka spĺňajúce kritéria definovane parametrom
     * query.Číselník je jednoznačne identifikovaný názvom. V názve číselníka nezáleží na veľkosti písmen a
     * na delenie slov v názve je možné použiť znak '_' alebo '-' t.j. napríklad názov DOCUMENT_TYPE je
     * ekvivalentný s document-type. Káždá hodnota obsahuje všetky atribúty. Ak chceš definovať zoznam
     * vrátených atribútov, tak použi {@link #getCodelist(java.lang.String, java.lang.String[])}. Ak číselník
     * z daným menom neexistuje, tak sa vyhodí výnimka {@link CodelistNotFoundException}.
     *
     * @param <T> codelist entry type
     * @param codelist názov číselníka
     * @param query query
     * @throws CodelistNotFoundException ak číselník neexistuje
     * @throws CompileException ak query nie je kompilovatelná
     * @return zoznam všetkých čiselníkových hodnôt
     */
    <T extends CodelistEntry> Codelist<T> getFilteredCodelist(String codelist, String query);


    /**
     * Vráti všetky platné čiselníkové hodnoty daného číselníka spĺňajúce kritéria definovane parametrom
     * query. Ak číselník z daným menom neexistuje, tak sa vyhodí výnimka {@link CodelistNotFoundException}.
     *
     * @param <T> typ číselníkka
     * @param query query
     * @param entryClass trieda číselníka
     * @throws CodelistNotFoundException ak číselník neexistuje
     * @return zoznam všetkých čiselníkových hodnôt
     */
    <T extends CodelistEntry> Codelist<T> getFilteredCodelist(Class<T> entryClass, String query);


    /**
     * Vráti všetky platné číselníkové hodnoty pre viaceré číselníky definované názvom. Táto metóda je vhodná
     * ak potrebuješ vrátiť naraz viacero číselníkov.
     *
     * @param codelists názvy číselníkov
     * @return číselníkové hodnoty
     */
    Map<String, Codelist<?>> getCodelists(String... codelists);


    /**
     * Vráti konkrétnu hodnotu číselníka definovanú atribútom {@link CodelistEntry#code}. Ak hodnota
     * neexistuje, tak {@link Optional#isPresent()} je false. Táto metóda vracia aj neplatné číselníkové
     * hodnoty. Ak číselník z daným menom neexistuje, tak sa vyhodí výnimka {@link CodelistNotFoundException},
     * alebo ak daná hodnota neexistuje, tak sa vyhodí výnkmka {@link EntryNotFoundException}.
     *
     * @param codelist názov číselníka
     * @param code primárny kľúč hodnoty
     * @throws CodelistNotFoundException ak číselník neexistuje
     * @throws EntryNotFoundException ak hodnota neexistuje
     * @return číselníková hodnota
     */
    CodelistEntry getEntry(String codelist, String code);


    /**
     * Vráti konkrétnu hodnotu číselníka definovanú atribútom {@link CodelistEntry#code}. Ak hodnota
     * neexistuje, tak {@link Optional#isPresent()} je false. Táto metóda vracia aj neplatné číselníkové
     * hodnoty. Ak číselník z daným menom neexistuje, tak sa vyhodí výnimka {@link CodelistNotFoundException},
     * alebo ak daná hodnota neexistuje, tak sa vyhodí výnkmka {@link EntryNotFoundException}.
     *
     * @param <T> trieda číselníka
     * @param code primárny kľúč hodnoty ako enumerácia
     * @throws CodelistNotFoundException ak číselník neexistuje
     * @throws EntryNotFoundException ak hodnota neexistuje
     * @return číselníková hodnota
     */
    <T extends CodelistEntry> T getEntry(CodelistEnum<T> code);


    /**
     * Vráti konkrétnu hodnotu číselníka definovanú atribútom {@link CodelistEntry#code}. Ak hodnota
     * neexistuje, tak {@link Optional#isPresent()} je false. Táto metóda vracia aj neplatné číselníkové
     * hodnoty. Ak daná hodnota neexistuje, tak sa vyhodí výnkmka {@link EntryNotFoundException}.
     *
     * @param <T> typ číselníkka
     * @param entryClass trieda reprezentujúca číselník
     * @param code primárny kľúč hodnoty
     * @throws CodelistNotFoundException ak číselník neexistuje
     * @throws EntryNotFoundException ak hodnota neexistuje
     * @return číselníková hodnota
     */
    <T extends CodelistEntry> T getEntry(Class<T> entryClass, String code);


    /**
     * Vráti číselníkové dáta ako zoznam máp (HashMap), kde každá inštancia mapy predstavuje 1 číselníkový
     * záznam. Táto reprezentácia môže byť vhodná napr. na serializáciu do json alebo iného formátu.
     *
     * @param codelist názov číselníka
     * @return číselníkové dáta ako zoznam máp
     */
    List<Map<String, Object>> getFlatEntries(String codelist);


    /**
     * Vráti používateľský názov {@link DataProvider} ak existuje alebo všeobecný názov.
     *
     * @return názov {@link DataProvider}
     */
    String getProviderName();


    /**
     * Načíta znova dáta daného číselníka a všetkých jeho závislostí.
     *
     * @param codelist
     */
    void reloadCache(String codelist);


    /**
     * Get ability to use mapping specific methods.
     *
     * @return entry mapper interface
     */
    EntryMapper getEntryMapper();


    /**
     * Zmaže všetky dáta z cache a znova načíta
     */
    void clearCache();

    public static class Builder {

        public static final String BASE_PACKAGE = CodelistEntry.class.getPackageName();

        private DataProvider dataProvider = null;
        private Set<String> prefetchedCodelists = null;
        private Set<String> whitelistPackages = new HashSet<>(Arrays.asList(BASE_PACKAGE));
        private long expiryTime = 10;
        private TimeUnit expiryTimeUnit = TimeUnit.MINUTES;
        private boolean shallowReferences = false;
        private boolean disableCache = false;


        public CodelistClient build() {
            if (dataProvider == null) {
                throw new IllegalArgumentException("No DataProvider provided!");
            }
            if (prefetchedCodelists == null) {
                prefetchedCodelists = dataProvider.readAllNames();
            }
            if (disableCache) {
                return new CodelistClientImpl(dataProvider, whitelistPackages, shallowReferences);
            } else {
                return new CachedCodelistClientImpl(dataProvider, prefetchedCodelists, expiryTime,
                        expiryTimeUnit, whitelistPackages, shallowReferences);
            }
        }


        /**
         * Nastav si, ktoré číselníky budú nahraté do cache ihned po vytvorení inštancie codelist client.
         * Číselníky sa nahrávajú asynchrónne, takže to teoreticky nespomalí štart tvojej aplikácie.
         * Vo východzom stave sa loadujú všetky číselníky.
         *
         * @param prefetchedCodelists zoznam číselníkov, ktoré budú nahrane do cache
         * @return builder
         */
        public Builder withPrefetched(String... prefetchedCodelists) {
            this.prefetchedCodelists = new HashSet<>(Arrays.asList(prefetchedCodelists));
            return this;
        }


        /**
         * Žiadne číselníky nebudú nahraté do cache po vytvorení inštancie codelist client.
         * Číselníky sa dotiahnú pri prvoj zavolani. Vo východzom stave sa loadujú všetky číselníky.
         *
         * @return builder
         */
        public Builder noPrefetched() {
            this.prefetchedCodelists = Collections.emptySet();
            return this;
        }


        /**
         * Vo východzom nastavení sa skenuje len východzí classpath
         * (camp.xit.codelist.client.model), čo môže mať za následok, že sa nenájdu
         * všetky číselníkove triedy aplikácie. Týmto nastavením povieš, že sa bude skenovať celý classpath.
         *
         * @return builder
         */
        public Builder scanFullClasspath() {
            this.whitelistPackages.clear();
            return this;
        }


        /**
         * Vo východzom nastavení sa skenuje kompletný classpath, čo môže mať za následok pomalší štart
         * aplikácie. Táto metóda prída definovane priestory k základnym menným priestorom.
         *
         * @param whitelistPackages zoznam packages, ktoré sa budú skenovať
         * @return builder
         */
        public Builder addScanPackages(String... whitelistPackages) {
            this.whitelistPackages.addAll(Arrays.asList(whitelistPackages));
            return this;
        }


        /**
         * Vo východzom nastavení sa skenuje kompletný classpath, čo môže mať za následok pomalší štart
         * aplikácie. Týmto nastavením povieš, v ktorých packages sa nachádza model alebo mapovenie modelu pre
         * číselníky. Pozor! Táto metóda zmaže základny namespace číselníkov a použijú sa iba tieto namespace.
         * Ak chceš iba pridať niektorý namespace k základným, tak použi metódu
         * {@link #addScanPackages(java.lang.String...)}
         *
         * @param whitelistPackages zoznam packages, ktoré sa budú skenovať
         * @return builder
         */
        public Builder addScanOnlyPackages(String... whitelistPackages) {
            return addScanOnlyPackages(false, whitelistPackages);
        }


        /**
         * Vo východzom nastavení sa skenuje kompletný classpath, čo môže mať za následok pomalší štart
         * aplikácie.Týmto nastavením povieš, v ktorých packages sa nachádza model alebo mapovenie modelu pre
         * číselníky.
         *
         * @param addBasePackages if true, then add also base packages
         * @param whitelistPackages zoznam packages, ktoré sa budú skenovať
         * @return builder
         */
        public Builder addScanOnlyPackages(boolean addBasePackages, String... whitelistPackages) {
            if (addBasePackages) this.whitelistPackages.addAll(Arrays.asList(BASE_PACKAGE));
            this.whitelistPackages.addAll(Arrays.asList(whitelistPackages));
            return this;
        }


        /**
         * Nastav ľubovoľnú implementáciu DataProvider. Ako východzia je použitá
         * {@link SimpleCsvDataProvider}.
         *
         * @param dataProvider data provider
         * @return builder
         */
        public Builder withDataProvider(DataProvider dataProvider) {
            this.dataProvider = dataProvider;
            return this;
        }


        /**
         * Nastav čas, ako často sa bude aktualizovať cache. Aktualizácia prebieha asynchrónne.
         *
         * @param time časový údaj
         * @param unit jednotka času pre časový údaj
         * @return
         */
        public Builder withExpiryTime(long time, TimeUnit unit) {
            this.expiryTime = time;
            this.expiryTimeUnit = unit;
            return this;
        }


        /**
         * Ak je táto možnosť zapnutá, tak codelist client nebude doťahovať žiadne referencie, len vytvorí
         * inštanciu s vyplneným atribútom code.
         *
         * @return builder
         */
        public Builder shallowReferences() {
            this.shallowReferences = true;
            return this;
        }


        /**
         * Ak je táto možnosť zapnutá, tak codelist client bude doťahovať všetky referencie, vrátane
         * transitívnych. Táto možnosť je východzia.
         *
         * @return builder
         */
        public Builder deepReferences() {
            this.shallowReferences = false;
            return this;
        }


        /**
         * Vypne cache číselníkov. Pri doťahovaní referencií bude použitá lokálna cache, ktorá má za úlohu
         * zabezpečiť, že každý číselník bude dotiahnutý práva raz.
         *
         * @return builder
         */
        public Builder disableCache() {
            this.disableCache = true;
            return this;
        }


        /**
         * Zapne cache číselníkov. Táto možnosť je východzia. Cache je použitá aj pri doťahovaní referencií.
         *
         * @return builder
         */
        public Builder enableCache() {
            this.disableCache = false;
            return this;
        }
    }
}
