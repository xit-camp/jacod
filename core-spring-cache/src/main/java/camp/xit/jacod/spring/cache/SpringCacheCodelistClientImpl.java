package camp.xit.jacod.spring.cache;

import camp.xit.jacod.impl.CodelistClientImpl;
import camp.xit.jacod.impl.ShallowRefProvider;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.ReferenceProvider;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

/**
 * Cached thread-safety implementation of CodelistClient
 *
 * @author Michal Hlavac
 */
public class SpringCacheCodelistClientImpl extends CodelistClientImpl {

    private static final Logger LOG = LoggerFactory.getLogger(SpringCacheCodelistClientImpl.class);
    private final Cache cache;
    private final Set<String> prefetchedCodelists;
    private final boolean reloadDependencies;
    private final Duration expiryTimeout;


    public SpringCacheCodelistClientImpl(DataProvider provider, Cache cache, Duration expiryTimeout,
            Set<String> prefetchedCodelists, Set<String> whitelistPackages, boolean shallowReferences, boolean reloadDependencies) {

        super(provider, whitelistPackages, shallowReferences);

        this.cache = cache;
        this.expiryTimeout = expiryTimeout;
        this.prefetchedCodelists = prefetchedCodelists;
        this.reloadDependencies = reloadDependencies;

        loadCodelists(prefetchedCodelists);
    }


    private void loadCodelists(Collection<String> codelists) {
        mapper.getSortedDependencies(codelists).forEach(codelist -> cache.get(codelist, getLoader(codelist)));
    }


    @Override
    public Codelist<? extends CodelistEntry> getCodelist(String codelist) {
        Tuple<Codelist<CodelistEntry>> tuple = cache.get(codelist, getLoader(codelist));
        return tuple != null ? tuple.value : null;
    }


    @Override
    public Codelist<? extends CodelistEntry> getCustomCodelist(Class<? extends CodelistEntry> entryClass) {
        String name = mapper.getEntryMetadata(entryClass).getCodelistName();
        return getCodelist(name);
    }


    Callable<Tuple<Codelist<CodelistEntry>>> getLoader(String codelist) {
        return () -> {
            Tuple<Codelist<CodelistEntry>> tuple = cache.get(codelist, Tuple.class);
            if (tuple == null) {
                tuple = new Tuple<>(readCodelist(codelist, -1));
                LOG.debug("Codelist {} sucessfully loaded", codelist);
            } else if (tuple.isInvalid(expiryTimeout)) {
                long lastReload = tuple.lastModified;
                tuple = new Tuple<>(readCodelist(codelist, lastReload));
                LOG.debug("Codelist {} sucessfully reloaded", codelist);
            }
            return tuple;
        };
    }


    @Override
    protected Codelist<CodelistEntry> readCodelist(String codelist, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : (c, e) -> getEntry(c, e);

        Optional<Class<? extends CodelistEntry>> entryClass = mapper.getEntryClass(codelist);

        if (reloadDependencies) {
            Collection<String> deps = entryClass.map(cl -> mapper.getCodelistDependencies(cl)).orElse(Collections.emptySet());
            LOG.info("[{}] Expiring cache of codelist {} dependencies: {}", providerName, codelist, deps);
            deps.forEach(e -> cache.evictIfPresent(e));
        }

        return readCodelist(codelist, lastReadTime, refProvider);
    }


    @Override
    protected Codelist<? extends CodelistEntry> readCustomCodelist(Class<? extends CodelistEntry> entryClass, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : (c, e) -> getEntry(c, e);

        Collection<String> deps = mapper.getCodelistDependencies(entryClass);
        if (reloadDependencies) {
            LOG.info("[{}] Expiring cache of codelist {} dependencies: {}", providerName, entryClass.getSimpleName(), deps);
            deps.forEach(e -> cache.evictIfPresent(e));
        }

        return readCustomCodelist(entryClass, lastReadTime, refProvider);
    }


    private void reloadUsagesOf(String codelist) {
        Collection<String> usages = mapper.getUsagesOf(codelist);
        LOG.info("[{}] Codelist {} changed, so reloading usages: {}", providerName, codelist, usages);
        usages.forEach(cache::evict);
        Set<String> toLoad = new HashSet<>(usages);

        loadCodelists(toLoad);
    }


    @Override
    public void reloadCache(String codelist) {
        Optional<Class<? extends CodelistEntry>> entryClass = mapper.getEntryClass(codelist);
        Collection<String> deps = entryClass.map(cl -> mapper.getAllDependencies(cl)).orElse(Collections.singleton(codelist));
        LOG.info("[{}] Reloading codelist {} dependencies: {}", providerName, codelist, deps);
        cache.invalidate();
        loadCodelists(prefetchedCodelists);
    }


    @Override
    public void clearCache() {
        cache.invalidate();
        cache.clear();
        LOG.info("[{}] Codelist cache is now empty", providerName);
    }
}
