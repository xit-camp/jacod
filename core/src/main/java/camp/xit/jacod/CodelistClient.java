package camp.xit.jacod;

import camp.xit.jacod.entry.parser.ast.CompileException;
import camp.xit.jacod.impl.CodelistClientImpl;
import static camp.xit.jacod.impl.EntryAnnotationProcessor.MAPPERS_FILE;
import camp.xit.jacod.impl.MappersReg;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.model.CodelistEnum;
import camp.xit.jacod.provider.DataProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

public interface CodelistClient {

    /**
     * Return codelist instance which contains all codelist entries. If codelist does not exist
     * {@link CodelistNotFoundException}.
     *
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
    Map<String, Codelist<? extends CodelistEntry>> getCodelists(String... codelists);


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
    <T extends CodelistEntry> T getEntry(String codelist, String code);


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

        protected DataProvider dataProvider = null;
        protected Set<String> prefetchedCodelists = null;
        protected Set<String> whitelistMapperPackages = new HashSet<>();
        protected boolean shallowReferences = false;
        protected Map<String, Class<? extends CodelistEntry>> codelistMapping = new HashMap<>();


        public CodelistClient build() {
            if (dataProvider == null) {
                throw new IllegalArgumentException("No DataProvider provided!");
            }
            if (prefetchedCodelists == null) {
                prefetchedCodelists = dataProvider.getCodelistNames();
            }
            return new CodelistClientImpl(dataProvider, getMappersReg(), shallowReferences);
        }


        protected MappersReg getMappersReg() {
            return new MappersReg(codelistMapping, loadMapperClasses(null), whitelistMapperPackages);
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


        public T codelists(Class<? extends CodelistEntry>... entryClasses) {
            Stream.of(entryClasses).forEach(clazz -> codelistMapping.put(clazz.getSimpleName(), clazz));
            return (T) this;
        }


        public T codelist(String customName, Class<? extends CodelistEntry> entryClass) {
            codelistMapping.put(customName, entryClass);
            return (T) this;
        }


        public T whitelistMapperPackages(String... packages) {
            whitelistMapperPackages.addAll(Arrays.asList(packages));
            return (T) this;
        }


        public T whitelistMapperPackages(Package... packages) {
            whitelistMapperPackages.addAll(Stream.of(packages).map(Package::getName).collect(toList()));
            return (T) this;
        }


        public T disableMappers() {
            whitelistMapperPackages.clear();
            whitelistMapperPackages.add(CodelistEntry.class.getPackageName());
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


        public static Set<Class<?>> loadMapperClasses(final ClassLoader classLoader) {
            final Set<Class<?>> result = new HashSet<>();
            try {
                final ClassLoader cl = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
                final Enumeration<URL> systemResources = cl.getResources(MAPPERS_FILE);
                while (systemResources.hasMoreElements()) {
                    InputStream in = systemResources.nextElement().openStream();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                        List<String> classNames = reader.lines().collect(toList());
                        for (String className : classNames) {
                            Class<?> clazz = cl.loadClass(className);
                            result.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Loaded Classes: " + result);
            return result;
        }
    }
}
