package camp.xit.jacod.spring.cache;

import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

public class CachedDataProvider implements DataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CachedDataProvider.class);

    private final DataProvider provider;
    private final Cache cache;
    private final String name;
    private final Duration expiryTimeout;


    public CachedDataProvider(DataProvider provider, Cache cache, Duration expiryTimeout) {
        this.provider = provider;
        this.name = provider.getName() + "@SpringCache";
        this.cache = cache;
        this.expiryTimeout = expiryTimeout;
    }


    @Override
    public Set<String> readAllNames() {
        return provider.readAllNames();
    }


    @Override
    public Class<? extends DataProvider> getProviderClass() {
        return provider.getProviderClass();
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        Tuple<Optional<List<EntryData>>> tuple = cache.get(codelist, getLoader(codelist));
        return tuple != null ? tuple.value : null;
    }


    Callable<Tuple<Optional<List<EntryData>>>> getLoader(String codelist) {
        return () -> {
            Tuple<Optional<List<EntryData>>> tuple = cache.get(codelist, Tuple.class);
            if (tuple == null) {
                long start = System.currentTimeMillis();
                tuple = new Tuple<>(provider.readEntries(codelist, -1));
                long duration = System.currentTimeMillis() - start;
                LOG.debug("Entry data for codelist {} sucessfully loaded in {} ms", codelist, duration);
            } else if (tuple.isInvalid(expiryTimeout)) {
                long lastReload = tuple.lastModified;
                long start = System.currentTimeMillis();
                tuple = new Tuple<>(provider.readEntries(codelist, lastReload));
                long duration = System.currentTimeMillis() - start;
                LOG.debug("Entry data for codelist {} sucessfully reloaded in {} ms", codelist, duration);
            }
            return tuple;
        };
    }
}
