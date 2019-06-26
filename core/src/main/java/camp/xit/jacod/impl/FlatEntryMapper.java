package camp.xit.jacod.impl;

import camp.xit.jacod.Embeddable;
import camp.xit.jacod.InvalidEntryException;
import camp.xit.jacod.NotNull;
import static camp.xit.jacod.impl.EntryMetadata.getReferenceType;
import camp.xit.jacod.impl.FieldMap.FieldMapping;
import static camp.xit.jacod.impl.ValueParser.parseCollectionOfSimple;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatEntryMapper {

    private static final Logger LOG = LoggerFactory.getLogger(FlatEntryMapper.class);

    private final Map<String, Class<? extends CodelistEntry>> advancedCodelists;
    private final Map<Class<? extends CodelistEntry>, EntryMetadata> metadataMap;


    public FlatEntryMapper(Map<String, Class<? extends CodelistEntry>> advancedCodelists,
            Map<Class<? extends CodelistEntry>, EntryMetadata> metadataMap) {

        this.advancedCodelists = advancedCodelists;
        this.metadataMap = metadataMap;
    }


    public Map<String, Object> mapEntryToFlat(String name, Class<? extends DataProvider> providerClass,
            EntryData entryData) {

        Class<? extends CodelistEntry> entryClass = advancedCodelists.containsKey(name)
                ? advancedCodelists.get(name) : CodelistEntry.class;

        EntryMetadata metadata = metadataMap.get(entryClass);

        return mapToEntryFlat(metadata, name, providerClass, entryData);
    }


    public List<Map<String, Object>> mapToFlat(String name, Class<? extends DataProvider> providerClass,
            List<EntryData> entries) {

        Class<? extends CodelistEntry> entryClass = advancedCodelists.containsKey(name)
                ? advancedCodelists.get(name) : CodelistEntry.class;

        EntryMetadata metadata = metadataMap.get(entryClass);

        return entries.stream()
                .map(values -> mapToEntryFlat(metadata, name, providerClass, values))
                .collect(Collectors.toList());
    }


    public Optional<List<Map<String, Object>>> mapToFlat(String name, DataProvider provider, long lastReadTime) {
        Class<? extends CodelistEntry> entryClass = advancedCodelists.containsKey(name)
                ? advancedCodelists.get(name) : CodelistEntry.class;

        Class<? extends DataProvider> providerClass = provider.getProviderClass();
        EntryMetadata metadata = metadataMap.get(entryClass);
        String codelistName = metadata.getProviderCodelistName(providerClass, name).orElse(name);

        Optional<List<EntryData>> entries = provider.readEntries(codelistName, lastReadTime);

        return entries.map(es -> es.stream()
                .map(values -> mapToEntryFlat(metadata, name, providerClass, values))
                .collect(Collectors.toList()));
    }


    private Map<String, Object> mapToEntryFlat(EntryMetadata metadata, String identifier,
            Class<? extends DataProvider> providerClass, EntryData entryData) {

        Map<String, Object> result = new HashMap<>();

        Map<Field, FieldMapping> fieldMap = metadata.getFieldMap(providerClass, identifier);
        for (Map.Entry<Field, FieldMapping> entry : fieldMap.entrySet()) {
            Field field = entry.getKey();
            Object value = mapToFieldValueFlat(field, entry.getValue(), providerClass, entryData, metadata, result);
            if (value != null) {
                result.put(field.getName(), value);
            }
        }
        return result;
    }


    private Object mapToFieldValueFlat(Field field, FieldMapping mapping, Class<? extends DataProvider> providerClass,
            EntryData data, EntryMetadata metadata, Map<String, Object> resultData) {

        Class<?> type = field.getType();
        String mappedField = mapping.getMappedField();
        List<String> values = data.getFieldValues(mappedField);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Reading field " + metadata.getCodelistName() + "." + field.getName()
                    + " using key " + mappedField + " from " + data);
        }

        values = values == null ? Collections.emptyList() : values;
        boolean collection = EntryMetadata.isCollection(field);

        if (collection && values != null && values.size() == 1) {
            String[] valuesArray = values.iterator().next().split(",");
            values = Arrays.stream(valuesArray).map(v -> v.trim()).collect(Collectors.toList());
        }

        boolean notNull = field.isAnnotationPresent(NotNull.class);
        Object result = null;
        if (!collection) {
            Optional<String> strValue = values.size() == 1 ? Optional.of(values.iterator().next()) : Optional.empty();
            if (notNull) {
                String defaultValue = field.getAnnotation(NotNull.class).defaultValue();
                strValue = strValue.or(() -> Optional.ofNullable(defaultValue.isEmpty() ? null : defaultValue));
            }
            // check for not null field
            if (notNull && !strValue.isPresent()) {
                String fld = field.getName();
                String cls = metadata.getCodelistName();
                throw new InvalidEntryException("You were hit by KIWIGENS-418. Field " + cls + "." + fld + " cannot be empty!");
            }
            try {
                if (strValue.isPresent() && LocalDate.class.isAssignableFrom(type)) {
                    String val = strValue.get();
                    if (val.length() > 10) {
                        result = LocalDate.parse(val, DateTimeFormatter.ISO_DATE_TIME);
                    } else {
                        result = LocalDate.parse(val, DateTimeFormatter.ISO_DATE);
                    }
                } else if (strValue.isPresent() && Integer.class.isAssignableFrom(type)) {
                    try {
                        result = Integer.parseInt(strValue.get());
                    } catch (NumberFormatException e) {
                        result = Double.valueOf(Double.parseDouble(strValue.get())).intValue();
                    }
                } else if (strValue.isPresent() && BigDecimal.class.isAssignableFrom(type)) {
                    result = new BigDecimal(strValue.get());
                } else if (strValue.isPresent() && Boolean.class.isAssignableFrom(type)) {
                    result = Boolean.parseBoolean(strValue.get());
                } else if (strValue.isPresent() && CodelistEntry.class.isAssignableFrom(type)) {
                    result = strValue.get();
                } else if (Enum.class.isAssignableFrom(type)) {
                    if (strValue.isPresent() && !strValue.get().isEmpty()) {
                        try {
                            result = Enum.valueOf((Class<? extends Enum>) type, strValue.get());
                        } catch (IllegalArgumentException e) {
                            LOG.warn("Cannot map enum value " + strValue + " of " + type.getName(), e);
                        }
                    }
                } else if (strValue.isPresent() && String.class.isAssignableFrom(type)) {
                    result = strValue.get();
                } else if (type.isAnnotationPresent(Embeddable.class)) {
                    EntryMetadata fm = metadata.getEmbeddedFor(field);
                    mapToEmbeddedFlat(type, mappedField, providerClass, fm, data, resultData);
                } else if (strValue.isPresent()) {
                    throw new RuntimeException("Cannot map value. Invalid property type " + type.getName()
                            + " for " + metadata.getCodelistName() + "." + field.getName());
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                String msg = "Cannot map value " + strValue + " to " + type.getName();
                LOG.warn(msg, e);
                throw new IllegalArgumentException(msg, e);
            }
        } else { // Collection
            Class<?> refType = getReferenceType(field);
            if (refType.isAnnotationPresent(Embeddable.class)) {
                EntryMetadata fm = metadata.getEmbeddedFor(field);
                mapToEmbeddedListFlat(refType, mapping.getMappedField(), providerClass, fm, data, resultData);
            } else if (CodelistEntry.class.isAssignableFrom(refType)) {
                result = values;
            } else {
                result = parseCollectionOfSimple(values, refType);
            }
        }
        return result;
    }


    private void mapToEmbeddedListFlat(Class<?> objClass, String fieldPrefix, Class<? extends DataProvider> providerClass,
            EntryMetadata metadata, EntryData entryData, Map<String, Object> resultData) {

        int firstValueSize = -1;
        Set<String> emptyFields = new HashSet<>();
        for (Map.Entry<Field, FieldMapping> entry : metadata.getFieldMap(providerClass).entrySet()) {
            Field field = entry.getKey();
            FieldMapping fieldMapping = fieldPrefix != null ? entry.getValue().addPrefix(fieldPrefix) : entry.getValue();
            List<String> stringValues = entryData.getFieldValues(fieldMapping.getMappedField(), true);

            if (stringValues.size() > 0) {
                if (firstValueSize == -1) firstValueSize = stringValues.size();
                if (stringValues.size() != firstValueSize) {
                    throw new RuntimeException("Invalid values count for collection "
                            + "of embedded objects for " + fieldPrefix);
                }
                List<Object> values = new ArrayList<>();
                for (String strValue : stringValues) {
                    EntryData tmpData = new EntryData(fieldMapping.getMappedField(), strValue);
                    Object value = mapToFieldValueFlat(field, fieldMapping, providerClass, tmpData, metadata, resultData);
                    values.add(value);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Setting value {} to class {} field {} type {}", values, objClass.getSimpleName(),
                            field.getName(), field.getType().getName());
                }
                resultData.put(addPrefix(fieldPrefix, field.getName()), values);
            } else {
                emptyFields.add(addPrefix(fieldPrefix, field.getName()));
            }
        }
        if (firstValueSize > 0) {
            List<String> nullValues = Arrays.asList(new String[firstValueSize]);
            emptyFields.forEach(f -> resultData.put(f, nullValues));
        }
    }


    private void mapToEmbeddedFlat(Class<?> objClass, String fieldPrefix, Class<? extends DataProvider> providerClass,
            EntryMetadata metadata, EntryData entryData, Map<String, Object> resultData) {

        for (Map.Entry<Field, FieldMapping> entry : metadata.getFieldMap(providerClass).entrySet()) {
            Field field = entry.getKey();
            FieldMapping mappedField = fieldPrefix != null ? entry.getValue().addPrefix(fieldPrefix) : entry.getValue();
            Object value = mapToFieldValueFlat(field, mappedField, providerClass, entryData, metadata, resultData);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting value {} to class {} field {} type {}", value, objClass.getSimpleName(),
                        field.getName(), field.getType().getName());
            }
            if (value != null) {
                resultData.put(addPrefix(fieldPrefix, field.getName()), value);
            }
        }
    }


    private String addPrefix(String prefix, String value) {
        return prefix + value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
