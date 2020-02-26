package camp.xit.jacod.provider;

import camp.xit.jacod.CodelistNotFoundException;
import camp.xit.jacod.impl.CodelistExceptionPropagator;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import static org.cache2k.expiry.ExpiryTimeValues.NEUTRAL;
import org.cache2k.integration.AdvancedCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedDataProvider implements DataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CachedDataProvider.class);

    private final DataProvider provider;
    private final Cache<String, Tuple<Optional<List<EntryData>>>> cache;


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
        return provider.getName() + "@Cached";
    }


    public CachedDataProvider(DataProvider provider, Duration expiryTimeout) {
        this.provider = provider;
        Cache2kBuilder cacheBuilder = new Cache2kBuilder<String, Tuple<Optional<List<EntryData>>>>() {
        }
                .expireAfterWrite(expiryTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .resilienceDuration(1, TimeUnit.MINUTES)
                .refreshAhead(true)
                .keepDataAfterExpired(true)
                .loader(getLoader())
                .enableJmx(true)
                .exceptionPropagator(new CodelistExceptionPropagator());
        this.cache = cacheBuilder.build();
    }


    private AdvancedCacheLoader<String, Tuple<Optional<List<EntryData>>>> getLoader() {
        return new AdvancedCacheLoader<String, Tuple<Optional<List<EntryData>>>>() {
            @Override
            public Tuple<Optional<List<EntryData>>> load(String key, long currentTime, CacheEntry<String, Tuple<Optional<List<EntryData>>>> currentEntry) throws Exception {
                boolean alreadyLoaded = currentEntry != null;
                boolean previousException = alreadyLoaded && currentEntry.getException() != null;
                long lastReadTime = alreadyLoaded && !previousException ? currentEntry.getValue().lastModified : NEUTRAL;
                String providerName = provider.getName();
                Tuple<Optional<List<EntryData>>> result;
                try {
                    Optional<List<EntryData>> value = provider.readEntries(key, lastReadTime);
                    result = new Tuple<>(value, currentTime, true);
                } catch (CodelistNotChangedException e) {
                    LOG.debug("[{}] Reload successful, but codelist {} did not change.", providerName, key);
                    result = currentEntry.getValue().updateLastModified(currentTime).setChangedByReload(false);
                } catch (CodelistNotFoundException e) {
                    if (alreadyLoaded) {
                        LOG.warn("[{}] Source system is probably down or codelist was removed. "
                                + "Returning old cached value for {}", providerName, key);
                        result = currentEntry.getValue().setChangedByReload(false);
                    } else {
                        LOG.error("[{}] Codelist {} not found or source system is down!", providerName, key);
                        throw e;
                    }
                }
                return result;
            }
        };
    }


    @Override
    public Optional<List<EntryData>> readEntries(String codelist, long lastReadTime) {
        Tuple<Optional<List<EntryData>>> tuple = cache.get(codelist);
        return tuple != null ? tuple.value : null;
    }

    private static class Tuple<T> {

        private final T value;
        private long lastModified;
        private boolean changedByReload;


        public Tuple(T value, long lastModified, boolean changedByReload) {
            this.value = value;
            this.lastModified = lastModified;
            this.changedByReload = changedByReload;
        }


        public Tuple<T> setChangedByReload(boolean changedByReload) {
            this.changedByReload = changedByReload;
            return this;
        }


        public Tuple<T> updateLastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }


        public void invalidate() {
            this.lastModified = Long.MIN_VALUE;
        }
    }
}
