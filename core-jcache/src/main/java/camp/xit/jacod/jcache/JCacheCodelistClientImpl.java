package camp.xit.jacod.jcache;

import camp.xit.jacod.impl.CodelistClientImpl;
import camp.xit.jacod.impl.MappersReg;
import camp.xit.jacod.impl.ShallowRefProvider;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.ReferenceProvider;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import javax.cache.Cache;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cached thread-safety implementation of CodelistClient
 *
 * @author Michal Hlavac
 */
public class JCacheCodelistClientImpl extends CodelistClientImpl {

    private static final Logger LOG = LoggerFactory.getLogger(JCacheCodelistClientImpl.class);
    private final Cache<String, Codelist<CodelistEntry>> cache;
    private final Set<String> prefetchedCodelists;
    private final boolean reloadDependencies;


    public JCacheCodelistClientImpl(DataProvider provider, Cache cache, Duration expiryTimeout,
            Set<String> prefetchedCodelists, MappersReg mappersReg, boolean shallowReferences, boolean reloadDependencies) {

        super(provider, mappersReg, shallowReferences);

        this.cache = cache;
        this.prefetchedCodelists = prefetchedCodelists;
        this.reloadDependencies = reloadDependencies;

        loadCodelists(prefetchedCodelists);
    }


    private void loadCodelists(Collection<String> codelists) {
        mapper.getSortedDependencies(codelists)
                .forEach(codelist -> cache.invoke(codelist, new ValueLoaderEntryProcessor(), getLoader(codelist)));
    }


    @Override
    public Codelist<? extends CodelistEntry> getCodelist(String codelist) {
        return cache.invoke(codelist, new ValueLoaderEntryProcessor(), getLoader(codelist));
    }


    @Override
    public Codelist<? extends CodelistEntry> getCustomCodelist(Class<? extends CodelistEntry> entryClass) {
        String name = mapper.getEntryMetadata(entryClass).getCodelistName();
        return getCodelist(name);
    }


    Callable<Codelist<CodelistEntry>> getLoader(String codelist) {
        return () -> {
            Codelist<CodelistEntry> result = readCodelist(codelist, -1);
            LOG.debug("Codelist {} sucessfully loaded", codelist);
            return result;
        };
    }


    @Override
    protected Codelist<CodelistEntry> readCodelist(String codelist, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : (c, e) -> getEntry(c, e);

        Optional<Class<? extends CodelistEntry>> entryClass = mapper.getEntryClass(codelist);

        if (reloadDependencies) {
            Collection<String> deps = entryClass.map(cl -> mapper.getCodelistDependencies(cl)).orElse(Collections.emptySet());
            LOG.info("[{}] Expiring cache of codelist {} dependencies: {}", providerName, codelist, deps);
            deps.forEach(e -> cache.remove(e));
        }

        return readCodelist(codelist, lastReadTime, refProvider);
    }


    @Override
    protected Codelist<? extends CodelistEntry> readCustomCodelist(Class<? extends CodelistEntry> entryClass, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : (c, e) -> getEntry(c, e);

        Collection<String> deps = mapper.getCodelistDependencies(entryClass);
        if (reloadDependencies) {
            LOG.info("[{}] Expiring cache of codelist {} dependencies: {}", providerName, entryClass.getSimpleName(), deps);
            deps.forEach(e -> cache.remove(e));
        }

        return readCustomCodelist(entryClass, lastReadTime, refProvider);
    }


    private void reloadUsagesOf(String codelist) {
        Collection<String> usages = mapper.getUsagesOf(codelist);
        LOG.info("[{}] Codelist {} changed, so reloading usages: {}", providerName, codelist, usages);
        usages.forEach(cache::remove);
        Set<String> toLoad = new HashSet<>(usages);

        loadCodelists(toLoad);
    }


    @Override
    public void reloadCache(String codelist) {
        Optional<Class<? extends CodelistEntry>> entryClass = mapper.getEntryClass(codelist);
        Collection<String> deps = entryClass.map(cl -> mapper.getAllDependencies(cl)).orElse(Collections.singleton(codelist));
        LOG.info("[{}] Reloading codelist {} dependencies: {}", providerName, codelist, deps);
        cache.clear();
        loadCodelists(prefetchedCodelists);
    }


    @Override
    public void clearCache() {
        if (provider instanceof CachedDataProvider) {
            ((CachedDataProvider) provider).clearCache();
            LOG.info("[{}] Cache of data provider is now empty", providerName);
        }
        cache.clear();
        LOG.info("[{}] Codelist cache is now empty", providerName);
    }

    private class ValueLoaderEntryProcessor implements EntryProcessor<String, Codelist<CodelistEntry>, Codelist<CodelistEntry>> {

        @Override
        public Codelist<CodelistEntry> process(MutableEntry<String, Codelist<CodelistEntry>> entry, Object... arguments) throws EntryProcessorException {
            Callable<Codelist<CodelistEntry>> valueLoader = (Callable<Codelist<CodelistEntry>>) arguments[0];
            if (entry.exists()) {
                return entry.getValue();
            } else {
                Codelist<CodelistEntry> value;
                try {
                    value = valueLoader.call();
                } catch (Exception ex) {
                    throw new EntryProcessorException("Value loader '" + valueLoader + "' failed "
                            + "to compute value for key '" + entry.getKey() + "'", ex);
                }
                entry.setValue(value);
                return value;
            }
        }
    }
}
