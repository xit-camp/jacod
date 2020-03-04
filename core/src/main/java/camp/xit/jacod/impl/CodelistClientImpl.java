package camp.xit.jacod.impl;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.EntryMapper;
import camp.xit.jacod.EntryNotFoundException;
import camp.xit.jacod.entry.QueryEntryGroup;
import static camp.xit.jacod.impl.ExceptionUtil.*;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.model.CodelistEnum;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.ReferenceProvider;
import java.util.*;
import static org.cache2k.expiry.ExpiryTimeValues.NEUTRAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Non-cached thread-safety implementation of CodelistClient
 *
 * @author Michal Hlavac
 */
public class CodelistClientImpl implements CodelistClient {

    private static final Logger LOG = LoggerFactory.getLogger(CodelistClientImpl.class);
    protected final DataProvider provider;
    protected final CodelistEntryMapper mapper;
    protected final String providerName;
    protected final boolean shallowReferences;


    public CodelistClientImpl(DataProvider provider, Set<String> whitelistPackages, boolean shallowReferences) {
        this.mapper = new CodelistEntryMapper(whitelistPackages.toArray(new String[0]));
        this.provider = provider;
        this.providerName = provider.getName();
        this.shallowReferences = shallowReferences;
    }


    protected Optional<Codelist<? extends CodelistEntry>> getCodelistInternal(String codelist) {
        return Optional.ofNullable(getCodelist(codelist));
    }


    protected Optional<Codelist<? extends CodelistEntry>> getCustomCodelistOpt(Class entryClass) {
        return Optional.ofNullable(getCustomCodelist(entryClass));
    }


    @Override
    public Codelist<? extends CodelistEntry> getCodelist(String codelist) {
        return readCodelist(codelist, NEUTRAL);
    }


    protected Codelist<? extends CodelistEntry> getCustomCodelist(Class<? extends CodelistEntry> entryClass) {
        return readCustomCodelist(entryClass, NEUTRAL);
    }


    @Override
    public <T extends CodelistEntry> Codelist<T> getCodelist(Class<T> entryClass) {
        boolean custom = mapper.isCustomCodelist(entryClass);
        Optional<Codelist<? extends CodelistEntry>> result
                = custom ? getCustomCodelistOpt(entryClass) : getCodelistInternal(entryClass.getSimpleName());
        return (Codelist<T>) result.orElseThrow(() -> notFoundException(entryClass, provider, mapper));
    }


    @Override
    public <T extends CodelistEntry> Codelist<T> getFilteredCodelist(String codelist, String query) {
        Class<T> entryClass = (Class<T>) mapper.getEntryClass(codelist).orElse(CodelistEntry.class);
        QueryEntryGroup<T> group = new QueryEntryGroup<>(entryClass, query);
        return group.getEntries((Codelist<T>) getCodelist(codelist));
    }


    @Override
    public <T extends CodelistEntry> Codelist<T> getFilteredCodelist(Class<T> entryClass, String query) {
        QueryEntryGroup<T> group = new QueryEntryGroup<>(entryClass, query);
        return group.getEntries(getCodelist(entryClass));
    }


    @Override
    public Map<String, Codelist<?>> getCodelists(String... codelists) {
        Map<String, Codelist<?>> result = new HashMap<>();
        for (String codelist : codelists) {
            Optional<Codelist<? extends CodelistEntry>> entries = getCodelistInternal(codelist);
            entries.ifPresent(es -> result.put(codelist, es));
        }
        return result;
    }


    @Override
    public CodelistEntry getEntry(String cdlName, String code) {
        Optional<Codelist<? extends CodelistEntry>> cdl = getCodelistInternal(cdlName);
        return cdl.map(es -> es.get(code)).orElseThrow(() -> new EntryNotFoundException(cdlName, code));
    }


    @Override
    public <T extends CodelistEntry> T getEntry(CodelistEnum<T> code) {
        Class<T> entryClass = code.getCodelistClass();
        Codelist<T> codelist = getCodelist(entryClass);
        return codelist.getEntry(code);
    }


    @Override
    public <T extends CodelistEntry> T getEntry(Class<T> entryClass, String code) {
        return getCodelist(entryClass).getEntry(code);
    }


    @Override
    public List<Map<String, Object>> getFlatEntries(String codelist) {
        return mapper.mapToFlat(codelist, provider, NEUTRAL)
                .orElseThrow(() -> notFoundException(codelist, provider, mapper));
    }


    @Override
    public EntryMapper getEntryMapper() {
        return mapper;
    }


    @Override
    public String getProviderName() {
        return providerName;
    }


    protected Codelist<CodelistEntry> readCodelist(String codelist, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : new LocalCachedRefProvider(this);
        return readCodelist(codelist, lastReadTime, refProvider);
    }


    protected Codelist<CodelistEntry> readCodelist(String codelist, long lastReadTime, ReferenceProvider refProvider) {
        LOG.debug("[{}] Reading codelist {}", providerName, codelist);
        Codelist<CodelistEntry> result = mapper.mapToCodelist(codelist, provider, lastReadTime, refProvider)
                .orElseThrow(() -> notFoundException(codelist, provider, mapper));
        LOG.info("[{}] Codelist {} fully loaded", providerName, codelist);
        return result;
    }


    protected Codelist<? extends CodelistEntry> readCustomCodelist(Class<? extends CodelistEntry> entryClass, long lastReadTime) {
        ReferenceProvider refProvider = shallowReferences ? new ShallowRefProvider(mapper) : new LocalCachedRefProvider(this);
        return readCustomCodelist(entryClass, lastReadTime, refProvider);
    }


    protected Codelist<? extends CodelistEntry> readCustomCodelist(Class<? extends CodelistEntry> entryClass,
            long lastReadTime, ReferenceProvider refProvider) {
        LOG.debug("[{}] Reading custom codelist {}", providerName, entryClass.getName());
        Codelist<? extends CodelistEntry> result = mapper.mapToCodelist(entryClass, provider, lastReadTime, refProvider)
                .orElseThrow(() -> notFoundException(entryClass, provider, mapper));
        LOG.info("[{}] Codelist {} fully loaded", providerName, entryClass.getSimpleName());
        return result;
    }


    @Override
    public void reloadCache(String codelist) {
    }


    @Override
    public void clearCache() {
    }
}
