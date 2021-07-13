package camp.xit.jacod.impl;

import static camp.xit.jacod.impl.CodelistAnnotationProcessor.CODELISTS_FILE;
import static camp.xit.jacod.impl.EntryAnnotationProcessor.MAPPERS_FILE;
import camp.xit.jacod.model.CodelistEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import static java.util.Collections.emptySet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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
        return loadResourceClasses(CODELISTS_FILE, null).stream()
                .filter(c -> CodelistEntry.class.isAssignableFrom(c))
                .map(c -> (Class<? extends CodelistEntry>) c)
                .collect(toMap(c -> c.getSimpleName(), c -> c));
    }


    private Set<Class<?>> loadMapperClasses() {
        return loadResourceClasses(MAPPERS_FILE, null);
    }


    private Set<Class<?>> loadResourceClasses(final String resource, final ClassLoader classLoader) {
        final Set<Class<?>> result = new HashSet<>();
        try {
            final ClassLoader cl = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
            final Enumeration<URL> systemResources = cl.getResources(resource);
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
        return result;
    }
}
