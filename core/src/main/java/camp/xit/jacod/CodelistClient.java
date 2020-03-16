package camp.xit.jacod;

import camp.xit.jacod.entry.parser.ast.CompileException;
import camp.xit.jacod.impl.CodelistClientImpl;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.model.CodelistEnum;
import camp.xit.jacod.provider.DataProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CodelistClient {

    /**
     * Return codelist instance which contains all codelist entries. If codelist does not exist
     * {@link CodelistNotFoundException}.
     *
     * @param <T> codelist type
     * @param codelist codelist name
     * @throws CodelistNotFoundException if codelist does not exist
     * @return codelist
     */
    Codelist<? extends CodelistEntry> getCodelist(String codelist);


    /**
     * Return codelist instance which contains all codelist entries. If codelist does not exist
     * {@link CodelistNotFoundException}.
     *
     * @param <T> codelist type
     * @param entryClass codelist entry class
     * @throws CodelistNotFoundException if codelist does not exist
     * @return codelist
     */
    <T extends CodelistEntry> Codelist<T> getCodelist(Class<T> entryClass);


    /**
     * Return codelist instance which contains only entries that are result of defined query. If codelist does
     * not exist {@link CodelistNotFoundException}.
     *
     * @param <T> codelist type
     * @param codelist codelist name
     * @param query query
     * @throws CodelistNotFoundException if codelist does not exist
     * @throws CompileException if query is invalid
     * @return codelist
     */
    <T extends CodelistEntry> Codelist<T> getFilteredCodelist(String codelist, String query);


    /**
     * Return codelist instance which contains only entries that are result of defined query. If codelist does
     * not exist {@link CodelistNotFoundException}.
     *
     * @param <T> codelist type
     * @param query query
     * @param entryClass codelist entry class
     * @throws CodelistNotFoundException if codelist does not exist
     * @return codelist
     */
    <T extends CodelistEntry> Codelist<T> getFilteredCodelist(Class<T> entryClass, String query);


    /**
     * Return defined subset of codelists as map. This is useful when you need fetch more than one codelist in
     * one call.
     *
     * @param codelists codelist names
     * @return map of codelists
     */
    Map<String, Codelist<?>> getCodelists(String... codelists);


    /**
     * Returns codelist entry value defined by {@link CodelistEntry#code} attribute. See {@link CodelistEnum}
     * to define enums. If codelist does not exist {@link CodelistNotFoundException} and if entry doesn't
     * exist, {@link EntryNotFoundException} is thrown. This method returns also invalid values.
     *
     * @param <T> type of codelist
     * @param code primary key of entry
     * @throws CodelistNotFoundException if codelist does not exist
     * @throws EntryNotFoundException if entry does not exist
     * @return codelist entry value
     */
    CodelistEntry getEntry(String codelist, String code);


    /**
     * Returns codelist entry value defined by {@link CodelistEntry#code} enumeration attribute. See
     * {@link CodelistEnum} to define enums. If codelist does not exist {@link CodelistNotFoundException} and
     * if entry doesn't exist, {@link EntryNotFoundException} is thrown. This method returns also invalid
     * values.
     *
     * @param <T> type of codelist
     * @param code primary key of entry
     * @throws CodelistNotFoundException if codelist does not exist
     * @throws EntryNotFoundException if entry does not exist
     * @return codelist entry value
     */
    <T extends CodelistEntry> T getEntry(CodelistEnum<T> code);


    /**
     * Returns codelist entry value defined by {@link CodelistEntry#code} attribute. If codelist does not
     * exist {@link CodelistNotFoundException} and if entry doesn't exist, {@link EntryNotFoundException} is
     * thrown. This method returns also invalid values.
     *
     * @param <T> type of codelist
     * @param entryClass codelist entry class
     * @param code primary key of entry
     * @throws CodelistNotFoundException if codelist does not exist
     * @throws EntryNotFoundException if entry does not exist
     * @return codelist entry value
     */
    <T extends CodelistEntry> T getEntry(Class<T> entryClass, String code);


    /**
     * Returns codelist entries as list of flat map implemented by {@link HashMap}, where every map in the
     * list is one codelist entry. It's useful to serialize data into another format e.g. JSON.
     *
     * @param codelist codelist name
     * @return codelist entries as list of flat maps.
     */
    List<Map<String, Object>> getFlatEntries(String codelist);


    /**
     * Returns data provider name defined by {@link DataProvider#getName()}.
     *
     * @return name of {@link DataProvider}
     */
    String getProviderName();


    /**
     * Reload cached codelist data.
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
     * Remove all data from cache
     */
    void clearCache();

    public static class Builder<T extends Builder> {

        public static final String[] BASE_PACKAGES = new String[]{CodelistEntry.class.getPackageName()};

        protected DataProvider dataProvider = null;
        protected Set<String> prefetchedCodelists = null;
        protected Set<String> whitelistPackages = new HashSet<>(Arrays.asList(BASE_PACKAGES));
        protected boolean shallowReferences = false;


        public CodelistClient build() {
            if (dataProvider == null) {
                throw new IllegalArgumentException("No DataProvider provided!");
            }
            if (prefetchedCodelists == null) {
                prefetchedCodelists = dataProvider.readAllNames();
            }
            return new CodelistClientImpl(dataProvider, whitelistPackages, shallowReferences);
        }


        /**
         * Define list of codelists that will be fetched right after construction of {@link CodelistClient}.
         * All codelists are prefetched by default. This setting is useful when you need to prefetch only
         * subset of all codelists.
         *
         * @param prefetchedCodelists zoznam číselníkov, ktoré budú nahrane do cache
         * @return builder
         */
        public T withPrefetched(String... prefetchedCodelists) {
            this.prefetchedCodelists = new HashSet<>(Arrays.asList(prefetchedCodelists));
            return (T) this;
        }


        /**
         * Žiadne číselníky nebudú nahraté do cache po vytvorení inštancie codelist client.
         * Číselníky sa dotiahnú pri prvoj zavolani. Vo východzom stave sa loadujú všetky číselníky.
         *
         * @return builder
         */
        public T noPrefetched() {
            this.prefetchedCodelists = Collections.emptySet();
            return (T) this;
        }


        /**
         * In the default setting, only the default classpath (camp.xit.jacod.model) is scanned, which may
         * result in not finding all application classes. This setting tells that the entire classpath will be
         * scanned.
         *
         * @return builder
         */
        public T scanFullClasspath() {
            this.whitelistPackages.clear();
            return (T) this;
        }


        /**
         * In the default setting, only the default classpath (camp.xit.jacod.model) is scanned, which may
         * result in not finding all application classes. This method adds defined packages, that will be
         * scanned.
         *
         * @param whitelistPackages list of packages, that will be scanned
         * @return builder
         */
        public T addScanPackages(String... whitelistPackages) {
            this.whitelistPackages.addAll(Arrays.asList(whitelistPackages));
            return (T) this;
        }


        /**
         * Set instance of data provider implementation. This is mandatory attribute. Application throws
         * {@link IllegalArgumentException} when no data provider is set.
         *
         * @param dataProvider data provider
         * @return builder
         */
        public T withDataProvider(DataProvider dataProvider) {
            this.dataProvider = dataProvider;
            return (T) this;
        }


        /**
         * Shallow references means, that {@link CodelistClient} wont fetch codelist references. It creates
         * references with filled code property. Default value: false
         *
         * @return builder
         */
        public T shallowReferences() {
            this.shallowReferences = true;
            return (T) this;
        }


        /**
         * As oposite to shallow references, this settings will fetch all references. This setting is default
         * behaviour of {@link CodelistClient}
         *
         * @return builder
         */
        public T deepReferences() {
            this.shallowReferences = false;
            return (T) this;
        }
    }
}
