package camp.xit.jacod.impl;

import camp.xit.jacod.AdvancedCodelistProvider;
import camp.xit.jacod.CodelistMappingProvider;
import camp.xit.jacod.model.CodelistEntry;
import static java.util.Collections.emptySet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MappersRef {

    private static final Logger LOG = LoggerFactory.getLogger(MappersRef.class);

    private final Map<String, Class<? extends CodelistEntry>> codelistMapping;
    private final Set<Class<?>> mapperClasses;


    public MappersRef() {
        this(emptySet());
    }


    public MappersRef(Set<String> whitelistPackages) {
        long start = System.currentTimeMillis();
        this.codelistMapping = loadCodelistClasses(whitelistPackages);
        this.mapperClasses = loadMapperClasses(whitelistPackages);
        long duration = System.currentTimeMillis() - start;
        LOG.info("Loaded {} codelists and {} mapper classes in {} ms", codelistMapping.size(), mapperClasses.size(), duration);
    }


    public Map<String, Class<? extends CodelistEntry>> getCodelistMapping() {
        return codelistMapping;
    }


    public Set<Class<?>> getMapperClasses() {
        return mapperClasses;
    }


    private boolean isAllowedClass(Set<String> whitelistPackages, Class<?> mapperClass) {
        return whitelistPackages.isEmpty() || whitelistPackages.contains(mapperClass.getPackageName());
    }


    private Map<String, Class<? extends CodelistEntry>> loadCodelistClasses(Set<String> whitelistPackages) {
        return ServiceLoader.load(AdvancedCodelistProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .flatMap(t -> t.getAdvancedCodelists().stream())
                .filter(c -> isAllowedClass(whitelistPackages, c))
                .collect(toMap(c -> c.getSimpleName(), c -> c));
    }


    private Set<Class<?>> loadMapperClasses(Set<String> whitelistPackages) {
        return ServiceLoader.load(CodelistMappingProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .flatMap(t -> t.getMapperClasses().stream())
                .filter(c -> isAllowedClass(whitelistPackages, c))
                .collect(toSet());
    }
}
