package camp.xit.jacod.jcache;

import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.cache.Cache;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedDataProvider implements DataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CachedDataProvider.class);

    private final DataProvider provider;
    private final Cache<String, List<EntryData>> cache;
    private final String name;


    public CachedDataProvider(DataProvider provider, Cache cache) {
        this.provider = provider;
        this.name = provider.getName() + "@JCache";
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
        List<EntryData> data = cache.invoke(codelist, new ValueLoaderEntryProcessor(), getLoader(codelist));
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

    private class ValueLoaderEntryProcessor implements EntryProcessor<String, List<EntryData>, List<EntryData>> {

        @Override
        public List<EntryData> process(MutableEntry<String, List<EntryData>> entry, Object... arguments) throws EntryProcessorException {
            Callable<List<EntryData>> valueLoader = (Callable<List<EntryData>>) arguments[0];
            if (entry.exists()) {
                return entry.getValue();
            } else {
                List<EntryData> value;
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


    public void clearCache() {
        cache.clear();
    }
}
