package camp.xit.jacod.impl;

import camp.xit.jacod.model.CodelistEntry;
import static java.util.Collections.emptySet;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toMap;
import java.util.stream.Stream;

public final class MappersReg {

    private final Map<String, Class<? extends CodelistEntry>> codelistMapping;
    private final Set<Class<?>> mapperClasses;
    private final Set<String> whitelistMapperPackages;


    public MappersReg(Map<String, Class<? extends CodelistEntry>> codelistMapping,
            Set<Class<?>> mapperClasses) {
        this(codelistMapping, mapperClasses, emptySet());
    }


    public MappersReg(Map<String, Class<? extends CodelistEntry>> codelistMapping,
            Set<Class<?>> mapperClasses, Set<String> whitelistMapperPackages) {
        this.codelistMapping = codelistMapping;
        this.mapperClasses = mapperClasses;
        this.whitelistMapperPackages = whitelistMapperPackages;
    }


    public Map<String, Class<? extends CodelistEntry>> getCodelistMapping() {
        return codelistMapping;
    }


    public Set<Class<?>> getMapperClasses() {
        return mapperClasses;
    }


    public Set<String> getWhitelistMapperPackages() {
        return whitelistMapperPackages;
    }


    public boolean isMapperClassAllowed(Class<?> mapperClass) {
        return whitelistMapperPackages.isEmpty() || whitelistMapperPackages.contains(mapperClass.getPackageName());
    }


    public final static Map<String, Class<? extends CodelistEntry>> mappingFromClasses(Class<? extends CodelistEntry>... entryClasses) {
        return Stream.of(entryClasses).collect(toMap(c -> c.getSimpleName(), c -> c));
    }
}
