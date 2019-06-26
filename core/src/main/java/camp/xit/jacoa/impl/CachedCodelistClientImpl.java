package camp.xit.jacoa.impl;

import camp.xit.jacoa.CodelistNotFoundException;
import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.CodelistEntry;
import camp.xit.jacoa.provider.CodelistNotChangedException;
import camp.xit.jacoa.provider.DataProvider;
import camp.xit.jacoa.provider.ReferenceProvider;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import org.cache2k.CacheOperationCompletionListener;
import org.cache2k.event.CacheEntryUpdatedListener;
import org.cache2k.expiry.ExpiryTimeValues;
import static org.cache2k.expiry.ExpiryTimeValues.NEUTRAL;
import org.cache2k.integration.AdvancedCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cached thread-safety implementation of CodelistClient
 *
 * @author Michal Hlavac
 */
public class CachedCodelistClientImpl extends CodelistClientImpl {

    private static final Logger LOG = LoggerFactory.getLogger(CachedCodelistClientImpl.class);
    private final Cache<String, Tuple<Codelist<CodelistEntry>>> cache;
    private final Set<String> prefetchedCodelists;


    public CachedCodelistClientImpl(DataProvider provider, Set<String> prefetchedCodelists, long expiryTime,
            TimeUnit expiryTimeUnit, Set<String> whitelistPackages, boolean shallowReferences) {
        super(provider, whitelistPackages, shallowReferences);
        this.prefetchedCodelists = prefetchedCodelists;
        this.cache = new Cache2kBuilder<String, Tuple<Codelist<CodelistEntry>>>() {
        }
                .expireAfterWrite(expiryTime, expiryTimeUnit)
                .resilienceDuration(1, TimeUnit.MINUTES)
                .refreshAhead(true)
                .keepDataAfterExpired(true)
                .loader(getLoader())
                .enableJmx(true)
//                .addAsyncListener(new CodelistUpdatedListener())
                .exceptionPropagator(new CodelistExceptionPropagator())
                .build();

        // prefetch all or selected codelists
        List<String> orderedPrefetched = mapper.getSortedDependencies(prefetchedCodelists);
        LOG.debug("Prefetching codelists: " + orderedPrefetched);
        // Do not use prefetchAll method while it stops prefetching on first cache access
        cache.loadAll(orderedPrefetched, new CacheOperationCompletionListener() {
            @Override
            public void onCompleted() {
                LOG.info("[{}] All prefetched codelists loaded", providerName);
            }


            @Override
            public void onException(Throwable e) {
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
        Collection<String> deps = entryClass.map(cl -> mapper.getCodelistDependencies(cl)).orElse(Collections.emptySet());
        LOG.info("[{}] Expiring cache of codelist {} dependencies: {}", providerName, codelist, deps);
        deps.forEach(e -> cache.expireAt(e, ExpiryTimeValues.NOW));

        return readCodelist(codelist, lastReadTime, refProvider);
    }


    @Override
    protected Codelist<? extends CodelistEntry> readCustomCodelist(Class<? extends CodelistEntry> entryClass, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : (c, e) -> getEntry(c, e);

        Collection<String> deps = mapper.getCodelistDependencies(entryClass);
        LOG.info("[{}] Expiring cache of codelist {} dependencies: {}", providerName, entryClass.getSimpleName(), deps);
        deps.forEach(e -> cache.expireAt(e, ExpiryTimeValues.NOW));

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
        cache.loadAll(toLoad, new CacheOperationCompletionListener() {
            @Override
            public void onCompleted() {
                LOG.info("[{}] Usages successfully reload for {}", providerName, codelist);
            }


            @Override
            public void onException(Throwable e) {
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
        cache.loadAll(Collections.singleton(codelist), null);
    }


    @Override
    public void clearCache() {
        cache.removeAll();
        LOG.info("[{}] Codelist cache is now empty", providerName);

        List<String> ordered = mapper.getSortedDependencies(prefetchedCodelists);
        cache.loadAll(ordered, new CacheOperationCompletionListener() {
            @Override
            public void onCompleted() {
                LOG.info("[{}] All codelists reloaded", providerName);
            }


            @Override
            public void onException(Throwable e) {
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
