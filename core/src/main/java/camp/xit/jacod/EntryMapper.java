package camp.xit.jacod;

import camp.xit.jacod.impl.FlatEntryMapper;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import camp.xit.jacod.provider.ReferenceProvider;
import java.io.PrintStream;
import java.util.*;

public interface EntryMapper {

    public boolean isAdvancedCodelist(String codelist);


    public FlatEntryMapper getFlatEntryMapper();


    public Optional<List<Map<String, Object>>> mapToFlat(String name, DataProvider provider, long lastReadTime);


    public List<Map<String, Object>> mapToFlat(String name, Class<? extends DataProvider> providerClass, List<EntryData> entries);


    public Map<String, Object> mapEntryToFlat(String name, Class<? extends DataProvider> providerClass, EntryData entry);


    public Optional<Codelist<CodelistEntry>> mapToShallowCodelist(String identifier, DataProvider provider);


    public <T extends CodelistEntry> Optional<Codelist<T>> mapToCodelist(Class<T> entryClass,
            DataProvider provider, long lastReadTime, ReferenceProvider refProvider);


    public Optional<Codelist<CodelistEntry>> mapToCodelist(String identifier, DataProvider provider,
            long lastReadTime, ReferenceProvider refProvider);


    public Codelist<CodelistEntry> mapToCodelist(String identifier, List<EntryData> entries,
            Class<? extends DataProvider> providerClass, ReferenceProvider refProvider);


    public CodelistEntry mapToEntry(String identifier, EntryData entryData,
            Class<? extends DataProvider> providerClass, ReferenceProvider refProvider);


    public boolean isCustomCodelist(Class<? extends CodelistEntry> entryClass);


    public Optional<String> getProviderName(String codelistName, Class<? extends DataProvider> providerClass);


    public Map<String, String> getReverseProviderNames(Class<? extends DataProvider> providerClass);


    public Set<String> getUsagesOf(String codelist);


    public Set<String> getUsagesOf(Class<? extends CodelistEntry> entryClass);


    public List<String> getSortedDependencies(Iterable<String> codelists);


    public Collection<String> getAllDependencies(Class<? extends CodelistEntry> entryClass);


    public Collection<String> getCodelistDependencies(Class<? extends CodelistEntry> entryClass);


    public Collection<Class<? extends CodelistEntry>> getCodelistClasses();


    public Optional<Class<? extends CodelistEntry>> getEntryClass(String codelist);


    public void printMapping(String codelist, Class<? extends DataProvider> providerClass, PrintStream out);


    public void printMapping(Class<? extends CodelistEntry> codelistClass, Class<? extends DataProvider> providerClass, PrintStream out);


    public String mappingToString(String codelist, Class<? extends DataProvider> providerClass);


    public String mappingToString(Class<? extends CodelistEntry> codelistClass, Class<? extends DataProvider> providerClass);

}
