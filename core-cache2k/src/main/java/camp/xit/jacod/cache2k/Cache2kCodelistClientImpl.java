package camp.xit.jacod.cache2k;

import camp.xit.jacod.CodelistNotFoundException;
import camp.xit.jacod.impl.CodelistClientImpl;
import camp.xit.jacod.impl.ShallowRefProvider;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.CodelistNotChangedException;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.ReferenceProvider;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import org.cache2k.addon.UniversalResiliencePolicy;
import org.cache2k.event.CacheEntryUpdatedListener;
import org.cache2k.expiry.ExpiryTimeValues;
import static org.cache2k.expiry.ExpiryTimeValues.NEUTRAL;
import org.cache2k.io.AdvancedCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cached thread-safety implementation of CodelistClient
 *
 * @author Michal Hlavac
 */
public class Cache2kCodelistClientImpl extends CodelistClientImpl {

    private static final Logger LOG = LoggerFactory.getLogger(Cache2kCodelistClientImpl.class);
    private final Cache<String, Tuple<Codelist<CodelistEntry>>> cache;
    private final Set<String> prefetchedCodelists;
    private final boolean reloadDependencies;


    public Cache2kCodelistClientImpl(DataProvider provider, Set<String> prefetchedCodelists, Duration expiryTime,
            Set<String> whitelistPackages, boolean shallowReferences, boolean reloadReferences, boolean reloadDependencies) {

        super(provider, whitelistPackages, shallowReferences);

        this.prefetchedCodelists = prefetchedCodelists;
        this.reloadDependencies = reloadDependencies;

        Cache2kBuilder cacheBuilder = new Cache2kBuilder<String, Tuple<Codelist<CodelistEntry>>>() {
        }
                .expireAfterWrite(expiryTime.toMillis(), TimeUnit.MILLISECONDS)
                // TODO: implement this
                // .resilienceDuration(1, TimeUnit.MINUTES)
                .setup(UniversalResiliencePolicy::enable)
                .refreshAhead(true)
                .keepDataAfterExpired(true)
                .loader(getLoader())
                .exceptionPropagator(new CodelistExceptionPropagator());

        if (reloadReferences) {
            cacheBuilder.addAsyncListener(new CodelistUpdatedListener());
        }
        this.cache = cacheBuilder.build();

        // prefetch all or selected codelists
        List<String> orderedPrefetched = mapper.getSortedDependencies(prefetchedCodelists);
        LOG.debug("Prefetching codelists: " + orderedPrefetched);
        // Do not use prefetchAll method while it stops prefetching on first cache access
        cache.loadAll(orderedPrefetched).whenComplete((f, e) -> {
            if (e == null) {
                LOG.info("[{}] All prefetched codelists loaded", providerName);
            } else {
                LOG.warn("[" + providerName + "] Cannot prefetch codelists data", e);
            }
        });
    }


    @Override
    public Codelist<? extends CodelistEntry> getCodelist(String codelist) {
        Tuple<Codelist<CodelistEntry>> tuple = cache.get(codelist);
        return tuple != null ? tuple.value : null;
    }


    @Override
    public Codelist<? extends CodelistEntry> getCustomCodelist(Class<? extends CodelistEntry> entryClass) {
        String name = mapper.getEntryMetadata(entryClass).getCodelistName();
        return getCodelist(name);
    }


    @Override
    protected Codelist<CodelistEntry> readCodelist(String codelist, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : (c, e) -> getEntry(c, e);

        Optional<Class<? extends CodelistEntry>> entryClass = mapper.getEntryClass(codelist);

        if (reloadDependencies) {
            Collection<String> deps = entryClass.map(cl -> mapper.getCodelistDependencies(cl)).orElse(Collections.emptySet());
            LOG.info("[{}] Expiring cache of codelist {} dependencies: {}", providerName, codelist, deps);
            deps.forEach(e -> cache.expireAt(e, ExpiryTimeValues.NOW));
        }

        return readCodelist(codelist, lastReadTime, refProvider);
    }


    @Override
    protected Codelist<? extends CodelistEntry> readCustomCodelist(Class<? extends CodelistEntry> entryClass, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : (c, e) -> getEntry(c, e);

        Collection<String> deps = mapper.getCodelistDependencies(entryClass);
        if (reloadDependencies) {
            LOG.info("[{}] Expiring cache of codelist {} dependencies: {}", providerName, entryClass.getSimpleName(), deps);
            deps.forEach(e -> cache.expireAt(e, ExpiryTimeValues.NOW));
        }

        return readCustomCodelist(entryClass, lastReadTime, refProvider);
    }


    private AdvancedCacheLoader<String, Tuple<Codelist<CodelistEntry>>> getLoader() {
        return new AdvancedCacheLoader<String, Tuple<Codelist<CodelistEntry>>>() {
            @Override
            public Tuple<Codelist<CodelistEntry>> load(String key, long currentTime, CacheEntry<String, Tuple<Codelist<CodelistEntry>>> currentEntry) throws Exception {
                boolean alreadyLoaded = currentEntry != null;
                boolean previousException = alreadyLoaded && currentEntry.getException() != null;
                long lastReadTime = alreadyLoaded && !previousException ? currentEntry.getValue().lastModified : NEUTRAL;
                Tuple<Codelist<CodelistEntry>> result;
                try {
                    Codelist<CodelistEntry> value = readCodelist(key, lastReadTime);
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


    private Set<String> getCacheKeys() {
        Set<String> result = new HashSet<>();
        cache.keys().forEach(result::add);
        return result;
    }


    private void reloadUsagesOf(String codelist) {
        Collection<String> usages = mapper.getUsagesOf(codelist);
        LOG.info("[{}] Codelist {} changed, so reloading usages: {}", providerName, codelist, usages);
        cache.removeAll(usages);
        Set<String> toLoad = new HashSet<>(usages);
        toLoad.retainAll(getCacheKeys());
        cache.loadAll(toLoad).whenComplete((f, e) -> {
            if (e == null) {
                LOG.info("[{}] Usages successfully reload for {}", providerName, codelist);
            } else {
                LOG.warn("[" + providerName + "] Error while realoading usages of " + codelist, e);
            }
        });
    }


    @Override
    public void reloadCache(String codelist) {
        Optional<Class<? extends CodelistEntry>> entryClass = mapper.getEntryClass(codelist);
        Collection<String> deps = entryClass.map(cl -> mapper.getAllDependencies(cl)).orElse(Collections.singleton(codelist));
        LOG.info("[{}] Reloading codelist {} dependencies: {}", providerName, codelist, deps);
        cache.removeAll(deps);
        cache.loadAll(Collections.singleton(codelist));
    }


    @Override
    public void clearCache() {
        if (provider instanceof CachedDataProvider) {
            ((CachedDataProvider) provider).clearCache();
            LOG.info("[{}] Cache of data provider is now empty", providerName);
        }
        cache.clear();
        LOG.info("[{}] Codelist cache is now empty", providerName);

        List<String> ordered = mapper.getSortedDependencies(prefetchedCodelists);
        cache.loadAll(ordered).whenComplete((f, e) -> {
            if (e == null) {
                LOG.info("[{}] All codelists reloaded", providerName);
            } else {
                LOG.warn("[" + providerName + "] Cannot prefetch codelists data", e);
            }
        });
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

    private class CodelistUpdatedListener implements CacheEntryUpdatedListener {

        @Override
        public void onEntryUpdated(Cache cache, CacheEntry currentEntry, CacheEntry entryWithNewData) {
            boolean changed = ((Tuple) entryWithNewData.getValue()).changedByReload;
            if (changed) {
                reloadUsagesOf((String) entryWithNewData.getKey());
            }
        }
    }
}
