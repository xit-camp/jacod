package camp.xit.jacod.impl;

import camp.xit.jacod.provider.DataProvider;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseEntryMetadata extends EntryMetadata {

    Map<String, Map<Class<? extends DataProvider>, FieldMap<Field>>> specificMapping;


    public BaseEntryMetadata(Class<?> entryClass, Map<Field, FieldMap.FieldMapping> fieldMap,
            Map<Class<? extends DataProvider>, FieldMap<Field>> providerMappedFields,
            Set<Field> refs, Map<Field, EntryMetadata> embedded,
            Map<String, Map<Class<? extends DataProvider>, FieldMap<Field>>> specificMapping) {

        super(entryClass, fieldMap, providerMappedFields, null, refs, embedded);
        this.specificMapping = specificMapping;
    }


    @Override
    public Map<Field, FieldMap.FieldMapping> getFieldMap(Class<? extends DataProvider> providerClass, String codelist) {
        return getSpecificFieldMap(providerClass, codelist)
                .or(() -> ofNullable(providerMappedFields.get(providerClass)))
                .map(FieldMap::getFields).orElse(fieldMap);
    }


    @Override
    public Optional<String> getProviderCodelistName(Class<? extends DataProvider> providerClass, String codelistName) {
        return getSpecificFieldMap(providerClass, codelistName)
                .map(FieldMap::getCustomName).orElse(super.getProviderCodelistName(providerClass, codelistName));
    }


    private Optional<FieldMap<Field>> getSpecificFieldMap(Class<? extends DataProvider> providerClass, String codelist) {
        Map<Class<? extends DataProvider>, FieldMap<Field>> pfm = specificMapping.get(codelist);
        return pfm != null ? Optional.ofNullable(pfm.get(providerClass)) : Optional.empty();
    }


    public Map<String, String> getProviderNames(Class<? extends DataProvider> providerClass) {
        return specificMapping.entrySet().stream()
                .filter(e -> e.getValue().containsKey(providerClass))
                .filter(e -> e.getValue().get(providerClass).getCustomName().isPresent())
                .collect(Collectors.toMap(e -> e.getValue().get(providerClass).getCustomName().get(), e -> e.getKey()));
    }
}
