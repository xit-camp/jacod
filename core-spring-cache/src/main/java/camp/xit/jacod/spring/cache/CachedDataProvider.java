package camp.xit.jacod.spring.cache;

import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.ofNullable;
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


    public CachedDataProvider(DataProvider provider, Cache cache) {
        this.provider = provider;
        this.name = provider.getName() + "@SpringCache";
        this.cache = cache;
    }


    @Override
    public Set<String> getCodelistNames() {
        return provider.getCodelistNames();
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
        List<EntryData> data = cache.get(codelist, getLoader(codelist));
        return ofNullable(data);
    }


    Callable<List<EntryData>> getLoader(String codelist) {
        return () -> {
            long start = System.currentTimeMillis();
            List<EntryData> result = provider.readEntries(codelist, -1).orElse(null);
            long duration = System.currentTimeMillis() - start;
            LOG.debug("Entry data for codelist {} sucessfully loaded in {} ms", codelist, duration);
            return result;
        };
    }


    public void clearCache() {
        cache.clear();
    }
}
