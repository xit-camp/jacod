package camp.xit.jacod.impl;

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
    private final Set<String> whitelistPackages;


    public MappersRef() {
        this(emptySet());
    }


    public MappersRef(Set<String> whitelistMapperPackages) {
        long start = System.currentTimeMillis();
        this.codelistMapping = loadCodelistClasses();
        this.mapperClasses = loadMapperClasses();
        this.whitelistPackages = whitelistMapperPackages;
        long duration = System.currentTimeMillis() - start;
        LOG.info("Loaded {} codelists and {} mapper classes in {} ms", codelistMapping.size(), mapperClasses.size(), duration);
    }


    public Map<String, Class<? extends CodelistEntry>> getCodelistMapping() {
        return codelistMapping;
    }


    public Set<Class<?>> getMapperClasses() {
        return mapperClasses;
    }


    public Set<String> getWhitelistPackages() {
        return whitelistPackages;
    }


    public boolean isAllowedClass(Class<?> mapperClass) {
        return whitelistPackages.isEmpty() || whitelistPackages.contains(mapperClass.getPackageName());
    }


    private Map<String, Class<? extends CodelistEntry>> loadCodelistClasses() {
        return ServiceLoader.load(AdvancedCodelistProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .flatMap(t -> t.getAdvancedCodelists().stream())
                .collect(toMap(c -> c.getSimpleName(), c -> c));
    }


    private Set<Class<?>> loadMapperClasses() {
        return ServiceLoader.load(CodelistMappingProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .flatMap(t -> t.getMapperClasses().stream())
                .collect(toSet());
    }
}
