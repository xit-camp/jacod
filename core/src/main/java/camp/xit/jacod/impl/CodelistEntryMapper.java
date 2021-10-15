package camp.xit.jacod.impl;

import camp.xit.jacod.BaseEntryMapping;
import camp.xit.jacod.Embeddable;
import camp.xit.jacod.EntryMapper;
import camp.xit.jacod.EntryMapping;
import camp.xit.jacod.EntryMappings;
import camp.xit.jacod.InvalidEntryException;
import camp.xit.jacod.NotNull;
import static camp.xit.jacod.impl.EntryMetadata.getReferenceType;
import camp.xit.jacod.impl.FieldMap.FieldMapping;
import static camp.xit.jacod.impl.ValueParser.*;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import camp.xit.jacod.provider.ReferenceProvider;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.*;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CodelistEntryMapper implements EntryMapper {

    private static final Logger LOG = LoggerFactory.getLogger(CodelistEntryMapper.class);

    private final Map<Class<?>, Map<Class<? extends DataProvider>, EntryMapping>> entryMappings;
    private final Map<String, Map<Class<? extends DataProvider>, BaseEntryMapping>> baseEntryMappings;
    private final Map<String, Class<? extends CodelistEntry>> advancedCodelists;
    private final Map<Class<? extends CodelistEntry>, EntryMetadata> metadataMap;
    private final BaseEntryMetadata baseEntryMetadata;


    public CodelistEntryMapper() {
        this(emptySet());
    }


    public CodelistEntryMapper(String... whitelistPackages) {
        this(new HashSet<>(Arrays.asList(whitelistPackages)));
    }


    public CodelistEntryMapper(Set<String> whitelistPackages) {
        this.advancedCodelists = new ConcurrentHashMap<>();
        this.entryMappings = new HashMap<>();
        this.baseEntryMappings = new HashMap();
        registerEntryMetadata(new MappersRef(whitelistPackages));
        this.baseEntryMetadata = createBaseEntryMetadata();
        this.metadataMap = createMetadataMap(this.advancedCodelists);
    }


    private void registerEntryMetadata(MappersRef mappersReg) {
        for (Map.Entry<String, Class<? extends CodelistEntry>> entry : mappersReg.getCodelistMapping().entrySet()) {
            String codelistName = entry.getKey();
            Class<? extends CodelistEntry> entryClass = entry.getValue();
            if (advancedCodelists.containsKey(codelistName)) {
                Class<? extends CodelistEntry> existing = advancedCodelists.get(codelistName);
                throw new RuntimeException("Duplicate codelist class declaration for name " + codelistName
                        + ". Conflicting classes: [" + existing.getName() + ", " + entryClass.getName() + "]");
            }
            advancedCodelists.put(codelistName, entryClass);
        }

        mappersReg.getMapperClasses()
                .stream()
                .forEach(mapperClass -> setBaseEntryMappings(baseEntryMappings, mapperClass));

        mappersReg.getMapperClasses()
                .stream()
                .forEach(mapperClass -> setEntryMappings(entryMappings, mapperClass));
    }


    private void setEntryMappings(Map<Class<?>, Map<Class<? extends DataProvider>, EntryMapping>> mappings, Class<?> clazz) {
        List<EntryMapping> mappingsAnn = new ArrayList<>();
        if (clazz.isAnnotationPresent(EntryMapping.class)) {
            mappingsAnn.add(clazz.getAnnotation(EntryMapping.class));
        }
        if (clazz.isAnnotationPresent(EntryMappings.class)) {
            mappingsAnn.addAll(Arrays.asList(clazz.getAnnotation(EntryMappings.class).value()));
        }
        for (EntryMapping mapping : mappingsAnn) {
            Class<?> entryClass = mapping.entryClass();
            Class<? extends DataProvider> providerClass = mapping.provider();
            if (CodelistEntry.class.isAssignableFrom(clazz) && CodelistEntry.class.equals(entryClass)) {
                entryClass = clazz;
            } else if (clazz.isAnnotationPresent(Embeddable.class)) {
                entryClass = clazz;
            }

            Map<Class<? extends DataProvider>, EntryMapping> entryMappings = mappings.get(entryClass);
            if (entryMappings == null) {
                entryMappings = new HashMap<>();
                mappings.put(entryClass, entryMappings);
            }
            if (entryMappings.containsKey(providerClass)) {
                String providerName = providerClass.getSimpleName();
                String entryName = entryClass.getSimpleName();
                throw new RuntimeException("Duplicate entry mapping for provider = " + providerName
                        + ", entry = " + entryName + ", class: " + clazz);
            }
            entryMappings.put(providerClass, mapping);
        }
    }


    private void setBaseEntryMappings(Map<String, Map<Class<? extends DataProvider>, BaseEntryMapping>> mappings, Class<?> clazz) {
        if (clazz.isAnnotationPresent(BaseEntryMapping.class)) {
            BaseEntryMapping mapping = clazz.getAnnotation(BaseEntryMapping.class);
            Class<? extends DataProvider> providerClass = mapping.provider();

            Map<Class<? extends DataProvider>, BaseEntryMapping> entryMappings = mappings.get(mapping.codelist());
            if (entryMappings == null) {
                entryMappings = new HashMap<>();
                mappings.put(mapping.codelist(), entryMappings);
            }
            if (entryMappings.containsKey(providerClass)) {
                String providerName = providerClass.getSimpleName();
                String entryName = mapping.codelist();
                throw new RuntimeException("Duplicate entry mapping for provider = " + providerName
                        + ", entry = " + entryName + ", class: " + clazz);
            }
            entryMappings.put(providerClass, mapping);
        }
    }


    private synchronized Map<Class<? extends CodelistEntry>, EntryMetadata> createMetadataMap(
            Map<String, Class<? extends CodelistEntry>> advancedCodelists) {

        Map<Class<? extends CodelistEntry>, EntryMetadata> result = new HashMap<>();
        // add generic codelist metadata
        result.put(CodelistEntry.class, baseEntryMetadata);

        advancedCodelists.values().forEach((codelistClass) -> {
            result.put(codelistClass, createCodelistMetadata(codelistClass));
        });
        return result;
    }


    private BaseEntryMetadata createBaseEntryMetadata() {
        return (BaseEntryMetadata) createCodelistMetadata(CodelistEntry.class);
    }


    private EntryMetadata createCodelistMetadata(Class<?> objClass) {
        boolean baseEntry = objClass.equals(CodelistEntry.class);

        Map<Class<? extends DataProvider>, FieldMap<String>> providerFieldMapping = new HashMap<>();
        Map<String, Map<Class<? extends DataProvider>, FieldMap<String>>> baseProviderFieldMapping = new HashMap<>();
        Map<Class<? extends DataProvider>, EntryMapping> mappings = entryMappings.get(objClass);

        if (mappings != null) {
            for (Map.Entry<Class<? extends DataProvider>, EntryMapping> entry : mappings.entrySet()) {
                EntryMapping entryMapping = entry.getValue();
                Map<String, FieldMapping> map = Arrays.stream(entryMapping.fields())
                        .collect(Collectors.toMap(a -> a.field(), a -> new FieldMapping(a)));
                FieldMap<String> fieldMap = new FieldMap<>(entryMapping, map);
                providerFieldMapping.put(entry.getKey(), fieldMap);
            }
        }

        if (baseEntry) {
            for (Map.Entry<String, Map<Class<? extends DataProvider>, BaseEntryMapping>> entry : baseEntryMappings.entrySet()) {
                for (Map.Entry<Class<? extends DataProvider>, BaseEntryMapping> subEntry : entry.getValue().entrySet()) {
                    BaseEntryMapping entryMapping = subEntry.getValue();
                    Map<String, FieldMapping> map = Arrays.stream(entryMapping.fields())
                            .collect(Collectors.toMap(a -> a.field(), a -> new FieldMapping(a)));
                    FieldMap<String> fieldMap = new FieldMap<>(entryMapping.resourceName(), map, false);
                    Map<Class<? extends DataProvider>, FieldMap<String>> pfm = baseProviderFieldMapping.get(entry.getKey());
                    if (pfm == null) {
                        pfm = new HashMap<>();
                        baseProviderFieldMapping.put(entry.getKey(), pfm);
                    }
                    pfm.put(subEntry.getKey(), fieldMap);
                }
            }
        }

        Map<Field, FieldMapping> fields = new HashMap<>();
        Map<Class<? extends DataProvider>, FieldMap<Field>> providerMappedFields = new HashMap<>();
        Map<String, Map<Class<? extends DataProvider>, FieldMap<Field>>> baseMappedFields = new HashMap();

        Set<Field> refs = new HashSet<>();
        Map<Field, EntryMetadata> embedded = new HashMap<>();

        List<Field> declaredFields = Stream.of(objClass.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers())).collect(toList());
        Set<Field> declaredFieldsSet = new HashSet<>(declaredFields);

        if (baseEntryMetadata != null && CodelistEntry.class.isAssignableFrom(objClass)) {
            declaredFields.addAll(baseEntryMetadata.getFields());
        }

        for (Field field : declaredFields) {
            field.setAccessible(true);

            String mappedField = field.getName();

            if (declaredFieldsSet.contains(field)) {
                fields.put(field, new FieldMapping(mappedField));
            }

            // provider mappers
            for (Map.Entry<Class<? extends DataProvider>, FieldMap<String>> entry : providerFieldMapping.entrySet()) {
                FieldMap<Field> pf = providerMappedFields.get(entry.getKey());
                if (pf == null) {
                    pf = new FieldMap<>(entry.getValue());
                    providerMappedFields.put(entry.getKey(), pf);
                }
                FieldMapping providerMappedField = entry.getValue().getFields().get(field.getName());
                Map<Field, FieldMapping> providerBaseMap = baseEntryMetadata != null ? baseEntryMetadata.getFieldMap(entry.getKey()) : null;
                FieldMapping parentMappedField = providerBaseMap != null ? providerBaseMap.get(field) : null;
                pf.getFields().put(field, providerMappedField != null ? providerMappedField
                        : parentMappedField != null ? parentMappedField : new FieldMapping(mappedField));
            }

            if (baseEntry) {
                for (Map.Entry<String, Map<Class<? extends DataProvider>, FieldMap<String>>> entry : baseProviderFieldMapping.entrySet()) {
                    Map<Class<? extends DataProvider>, FieldMap<Field>> pfm = baseMappedFields.get(entry.getKey());
                    if (pfm == null) {
                        pfm = new HashMap<>();
                        baseMappedFields.put(entry.getKey(), pfm);
                    }
                    for (Map.Entry<Class<? extends DataProvider>, FieldMap<String>> subEntry : entry.getValue().entrySet()) {
                        FieldMap<Field> pf = pfm.get(subEntry.getKey());
                        if (pf == null) {
                            pf = new FieldMap<>(subEntry.getValue());
                            pfm.put(subEntry.getKey(), pf);
                        }
                        FieldMapping providerMappedField = subEntry.getValue().getFields().get(field.getName());
                        FieldMap<Field> fieldMap = providerMappedFields.get(subEntry.getKey());
                        Map<Field, FieldMapping> providerBaseMap = fieldMap != null ? fieldMap.getFields() : null;
                        FieldMapping parentMappedField = providerBaseMap != null ? providerBaseMap.get(field) : null;
                        pf.getFields().put(field, providerMappedField != null ? providerMappedField
                                : parentMappedField != null ? parentMappedField : new FieldMapping(mappedField));
                    }
                }
            }

            // handle references
            Class<?> refFieldType = EntryMetadata.getReferenceType(field);
            if (CodelistEntry.class.isAssignableFrom(refFieldType)) {
                refs.add(field);
            }
            if (refFieldType.isAnnotationPresent(Embeddable.class)) {
                embedded.put(field, createCodelistMetadata(refFieldType));
                refs.add(field);
            }
        }
        if (!baseEntry) {
            BaseEntryMetadata parentMetadata = CodelistEntry.class.isAssignableFrom(objClass) ? baseEntryMetadata : null;
            return new EntryMetadata(objClass, fields, providerMappedFields, parentMetadata, refs, embedded);
        } else {
            return new BaseEntryMetadata(objClass, fields, providerMappedFields, refs, embedded, baseMappedFields);
        }
    }


    @Override
    public final boolean isAdvancedCodelist(String codelist) {
        return advancedCodelists.containsKey(codelist);
    }


    @Override
    public FlatEntryMapper getFlatEntryMapper() {
        return new FlatEntryMapper(advancedCodelists, metadataMap);
    }


    @Override
    public Optional<List<Map<String, Object>>> mapToFlat(String name, DataProvider provider, long lastReadTime) {
        return getFlatEntryMapper().mapToFlat(name, provider, lastReadTime);
    }


    @Override
    public List<Map<String, Object>> mapToFlat(String name, Class<? extends DataProvider> providerClass, List<EntryData> entries) {
        return getFlatEntryMapper().mapToFlat(name, providerClass, entries);
    }


    @Override
    public Map<String, Object> mapEntryToFlat(String name, Class<? extends DataProvider> providerClass, EntryData entry) {
        return getFlatEntryMapper().mapEntryToFlat(name, providerClass, entry);
    }


    @Override
    public Optional<Codelist<CodelistEntry>> mapToShallowCodelist(String identifier,
            DataProvider provider) {
        ReferenceProvider refProvider = new ShallowRefProvider(this);
        return mapToCodelist(identifier, provider, -1, refProvider);
    }


    @Override
    public <T extends CodelistEntry> Optional<Codelist<T>> mapToCodelist(Class<T> entryClass,
            DataProvider provider, long lastReadTime, ReferenceProvider refProvider) {

        Class<? extends DataProvider> providerClass = provider.getProviderClass();
        EntryMetadata metadata = getEntryMetadata(entryClass);
        String codelistName = metadata.getCodelistName();
        String providerSpecificName = metadata.getProviderCodelistName(providerClass, codelistName).orElse(codelistName);
        Optional<List<EntryData>> entries = provider.readEntries(providerSpecificName, lastReadTime);
        return entries.map(es -> es.stream()
                .map(values -> mapToEntry(entryClass, codelistName, metadata, providerClass, values, refProvider))
                .collect(Codelist.collect(codelistName)));
    }


    @Override
    public Optional<Codelist<CodelistEntry>> mapToCodelist(String identifier, DataProvider provider,
            long lastReadTime, ReferenceProvider refProvider) {

        Class<? extends CodelistEntry> entryClass = getEntryClass(identifier).orElse(CodelistEntry.class);

        Class<? extends DataProvider> providerClass = provider.getProviderClass();
        EntryMetadata metadata = getEntryMetadata(entryClass);
        Optional<String> codelistName = metadata.getProviderCodelistName(providerClass, identifier);

        Optional<List<EntryData>> entries = provider.readEntries(codelistName.orElse(identifier), lastReadTime);

        return entries.map(es -> es.stream()
                .map(values -> mapToEntry(entryClass, identifier, metadata, providerClass, values, refProvider))
                .collect(Codelist.collect(identifier)));
    }


    @Override
    public Codelist<CodelistEntry> mapToCodelist(String identifier, List<EntryData> entries,
            Class<? extends DataProvider> providerClass, ReferenceProvider refProvider) {

        Class<? extends CodelistEntry> entryClass = advancedCodelists.containsKey(identifier)
                ? advancedCodelists.get(identifier) : CodelistEntry.class;

        EntryMetadata metadata = getEntryMetadata(entryClass);

        return entries.stream()
                .map(values -> mapToEntry(entryClass, identifier, metadata, providerClass, values, refProvider))
                .collect(Codelist.collect(identifier));
    }


    @Override
    public CodelistEntry mapToEntry(String identifier, EntryData entryData,
            Class<? extends DataProvider> providerClass, ReferenceProvider refProvider) {

        Class<? extends CodelistEntry> entryClass = advancedCodelists.containsKey(identifier)
                ? advancedCodelists.get(identifier) : CodelistEntry.class;

        EntryMetadata metadata = getEntryMetadata(entryClass);

        return mapToEntry(entryClass, identifier, metadata, providerClass, entryData, refProvider);
    }


    private <T extends CodelistEntry> T mapToEntry(Class<T> entryClass, String identifier, EntryMetadata metadata,
            Class<? extends DataProvider> providerClass, EntryData entryData, ReferenceProvider refProvider) {

        T instance = null;
        try {
            Constructor<T> cr = entryClass.getDeclaredConstructor();
            cr.setAccessible(true);
            instance = cr.newInstance();

            Map<Field, FieldMapping> fieldMap = metadata.getFieldMap(providerClass, identifier);

            for (Map.Entry<Field, FieldMapping> entry : fieldMap.entrySet()) {
                Field field = entry.getKey();
                Object value = mapToFieldValue(field, entry.getValue(), providerClass, identifier, entryData, metadata, refProvider);
                field.set(instance, value);
            }
        } catch (InvocationTargetException ite) {
            LOG.error("Can't create instance of CodelistEntry", ite.getTargetException());
            throw new IllegalStateException(ite.getTargetException());
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            LOG.error("Can't create instance of CodelistEntry", e);
            throw new IllegalStateException(e);
        }
        return instance;
    }


    private List<Object> mapToEmbeddedList(Class<?> objClass, String fieldPrefix, Class<? extends DataProvider> providerClass,
            EntryMetadata metadata, EntryData entryData, ReferenceProvider refProvider) {

        List<Object> instances = new ArrayList<>();
        try {
            int idx = 0;
            int firstValueSize = -1;
            do {
                Constructor<?> cr = objClass.getDeclaredConstructor();
                cr.setAccessible(true);
                Object instance = cr.newInstance();
                for (Map.Entry<Field, FieldMapping> entry : metadata.getFieldMap(providerClass).entrySet()) {
                    Field field = entry.getKey();
                    FieldMapping fieldMapping = entry.getValue();
                    fieldMapping = fieldPrefix != null ? fieldMapping.addPrefix(fieldPrefix) : fieldMapping;
                    String codelist = objClass.getSimpleName();

                    String mappedField = fieldMapping.getMappedField();
                    List<String> values = entryData.getFieldValues(mappedField, true);
                    if (!values.isEmpty()) {
                        if (firstValueSize == -1) firstValueSize = values.size();
                        if (values.size() != firstValueSize) {
                            throw new RuntimeException("Invalid values count for collection "
                                    + "of embedded objects for " + fieldPrefix + ". EntryData: " + entryData);
                        }

                        if (idx < firstValueSize) {
                            EntryData tmpData = new EntryData(mappedField, values.get(idx));

                            Object value = mapToFieldValue(field, fieldMapping, providerClass, codelist, tmpData, metadata, refProvider);
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Setting value {} to class {} field {} type {}", value, objClass.getSimpleName(),
                                        field.getName(), field.getType().getName());
                            }
                            if (value != null) {
                                field.set(instance, value);
                            }
                        }
                    }
                }
                if (idx < firstValueSize) instances.add(instance);
                idx++;
            } while (idx < firstValueSize);
        } catch (InvocationTargetException ite) {
            LOG.error("Can't create instance of CodelistEntry", ite.getTargetException());
            throw new IllegalStateException(ite.getTargetException());
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            LOG.error("Can't create instance of CodelistEntry", e);
            throw new IllegalStateException(e);
        }
        return instances;
    }


    private Object mapToEmbedded(Class<?> objClass, String fieldPrefix, Class<? extends DataProvider> providerClass,
            EntryMetadata metadata, EntryData entryData, ReferenceProvider refProvider) {

        Object instance = null;
        boolean hasValue = false;
        try {
            Constructor<?> cr = objClass.getDeclaredConstructor();
            cr.setAccessible(true);
            instance = cr.newInstance();

            for (Map.Entry<Field, FieldMapping> entry : metadata.getFieldMap(providerClass).entrySet()) {
                Field field = entry.getKey();
                FieldMapping fieldMapping = entry.getValue();
                fieldMapping = fieldPrefix != null ? fieldMapping.addPrefix(fieldPrefix) : fieldMapping;
                String codelist = objClass.getSimpleName();
                Object value = mapToFieldValue(field, fieldMapping, providerClass, codelist, entryData, metadata, refProvider);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Setting value {} to class {} field {} type {}", value, objClass.getSimpleName(),
                            field.getName(), field.getType().getName());
                }
                if (value != null) {
                    hasValue = true;
                    field.set(instance, value);
                }
            }
        } catch (InvocationTargetException ite) {
            LOG.error("Can't create instance of CodelistEntry", ite.getTargetException());
            throw new IllegalStateException(ite.getTargetException());
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            LOG.error("Can't create instance of CodelistEntry", e);
            throw new IllegalStateException(e);
        }
        return hasValue ? instance : null;
    }


    private Object mapToFieldValue(Field field, FieldMapping mapping, Class<? extends DataProvider> providerClass,
            String codelist, EntryData data, EntryMetadata metadata, ReferenceProvider refProvider) {

        Class<?> type = field.getType();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Reading field " + metadata.getCodelistName() + "." + field.getName() + " using key "
                    + mapping.getMappedField() + " from " + data);
        }

        boolean collection = EntryMetadata.isCollection(field);
        List<String> values = data.getFieldValues(mapping.getMappedField(), collection);

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
                throw new InvalidEntryException("Value of field " + codelist + "." + fld
                        + " cannot be empty! Mapped from: " + mapping.getMappedField() + " | Data: " + data);
            }
            try {
                if (strValue.isPresent() && isSimpleType(type)) {
                    result = parseSimpleValue(strValue.get(), type);
                } else if (strValue.isPresent() && CodelistEntry.class.isAssignableFrom(type)) {
                    final String cdlName = metadata.getReferenceName(field);
                    if (mapping.isLookupRef()) {
                        result = refProvider.provide(cdlName, strValue.get());
                    } else {
                        result = createShallowInstance((Class<CodelistEntry>) type, strValue.get());
                    }
                } else if (Enum.class.isAssignableFrom(type)) {
                    if (strValue.isPresent() && !strValue.get().isEmpty()) {
                        try {
                            result = Enum.valueOf((Class<? extends Enum>) type, strValue.get());
                        } catch (IllegalArgumentException e) {
                            LOG.warn("Cannot map enum value " + strValue + " of " + type.getName(), e);
                        }
                    }
                } else if (type.isAnnotationPresent(Embeddable.class)) {
                    EntryMetadata fm = metadata.getEmbeddedFor(field);
                    result = mapToEmbedded(type, mapping.getMappedField(), providerClass, fm, data, refProvider);
                } else if (strValue.isPresent()) {
                    throw new RuntimeException("Cannot map value. Invalid property type " + type.getName()
                            + " for " + metadata.getReferenceName(field) + "." + field.getName());
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                LOG.warn("Unable to map value " + strValue + " to type " + type.getName()
                        + " for field " + field.getName() + " using data " + data + " codelist", e);
                throw e;
            }
        } else { // Collection
            Class<?> refType = getReferenceType(field);
            if (isSimpleType(refType)) {
                result = parseCollectionOfSimple(values, refType);
            } else if (CodelistEntry.class.isAssignableFrom(refType)) {
                String ref = metadata.getReferenceName(field);
                result = values.stream()
                        .map(v -> mapping.isLookupRef() ? refProvider.provide(ref, v) : createShallowInstance(CodelistEntry.class, v))
                        .collect(Collectors.toList());
            } else if (refType.isAnnotationPresent(Embeddable.class)) {
                EntryMetadata fm = metadata.getEmbeddedFor(field);
                result = mapToEmbeddedList(refType, mapping.getMappedField(), providerClass, fm, data, refProvider);
            } else {
                throw new RuntimeException("Collection mapping to unsupported class " + refType.getName());
            }
        }
        return result;
    }


    @Override
    public final boolean isCustomCodelist(Class<? extends CodelistEntry> entryClass) {
        return !CodelistEntry.class.getPackage().equals(entryClass.getPackage());
    }


    public <T extends CodelistEntry> EntryMetadata getEntryMetadata(Class<T> clazz) {
        EntryMetadata metadata = metadataMap.get(clazz);
        if (metadata == null) {
            throw new IllegalArgumentException("Cannot find entry metadata for " + clazz.getName());
        }
        return metadata;
    }


    @Override
    public Optional<String> getProviderName(String codelistName, Class<? extends DataProvider> providerClass) {
        Class<? extends CodelistEntry> entryClass = getEntryClass(codelistName).orElse(CodelistEntry.class);

        EntryMetadata metadata = getEntryMetadata(entryClass);
        return metadata.getProviderCodelistName(providerClass, codelistName);
    }


    @Override
    public final Map<String, String> getReverseProviderNames(Class<? extends DataProvider> providerClass) {
        final Map<String, String> result = new HashMap<>();
        for (EntryMetadata metadata : metadataMap.values()) {
            Optional<String> providerName = metadata.getProviderCodelistName(providerClass, null);
            providerName.ifPresent(pn -> result.put(pn, metadata.getCodelistName()));
        }
        // add base sentries
        result.putAll(baseEntryMetadata.getProviderNames(providerClass));
        return result;
    }


    @Override
    public Set<String> getUsagesOf(String codelist) {
        DirectedGraph<String> graph = getDependencyGraph(metadataMap.keySet());
        return graph.edgesFrom(codelist);
    }


    @Override
    public Set<String> getUsagesOf(Class<? extends CodelistEntry> entryClass) {
        String codelist = getEntryMetadata(entryClass).getCodelistName();
        return getUsagesOf(codelist);
    }


    @Override
    public List<String> getSortedDependencies(Iterable<String> codelists) {
        return getDependencyGraphFromNames(codelists).sort();
    }


    DirectedGraph<String> getDependencyGraph(Iterable<Class<? extends CodelistEntry>> entryClasses) {
        DirectedGraph<String> result = new DirectedGraph<>();
        for (Class<? extends CodelistEntry> entryClass : entryClasses) {
            recursiveGraphReferences(entryClass, getEntryMetadata(entryClass), null, result);
        }
        return result;
    }


    DirectedGraph<String> getDependencyGraphFromNames(Iterable<String> codelists) {
        DirectedGraph<String> result = new DirectedGraph<>();
        for (String codelist : codelists) {
            Optional<Class<? extends CodelistEntry>> entryClass = getEntryClass(codelist);
            entryClass.ifPresentOrElse(cl -> recursiveGraphReferences(cl, getEntryMetadata(cl), null, result),
                    () -> result.addNode(codelist));
        }
        return result;
    }


    private void recursiveGraphReferences(Class<?> entryClass, EntryMetadata metadata,
            Class<? extends CodelistEntry> parentEntryClass, DirectedGraph<String> graph) {
        if (metadata != null && !entryClass.equals(CodelistEntry.class)) {
            if (CodelistEntry.class.isAssignableFrom(entryClass)) {
                parentEntryClass = (Class<? extends CodelistEntry>) entryClass;
                graph.addNode(metadata.getCodelistName());
            }
            for (Field field : metadata.getReferencies()) {
                Class<?> refType = metadata.getReferenceType(field);
                if (CodelistEntry.class.isAssignableFrom(refType)) {
                    String refCodelist = metadata.getReferenceName(field);
                    String parentCodelistName = metadataMap.get(parentEntryClass).getCodelistName();
                    graph.addEdge(refCodelist, parentCodelistName);
                    if (!refType.equals(CodelistEntry.class)) {
                        Class<? extends CodelistEntry> entryRefType = (Class<? extends CodelistEntry>) refType;
                        recursiveGraphReferences(entryRefType, getEntryMetadata(entryRefType), parentEntryClass, graph);
                    }
                }
                if (refType.isAnnotationPresent(Embeddable.class)) {
                    EntryMetadata emeta = getEntryMetadata(parentEntryClass).getEmbeddedFor(field);
                    recursiveGraphReferences(refType, emeta, parentEntryClass, graph);
                }
            }
        }
    }


    @Override
    public Collection<String> getAllDependencies(Class<? extends CodelistEntry> entryClass) {
        return getDependencyGraph(singleton(entryClass)).sort();
    }


    @Override
    public Collection<String> getCodelistDependencies(Class<? extends CodelistEntry> entryClass) {
        Set<String> excluded = singleton(getEntryMetadata(entryClass).getCodelistName());
        return getDependencyGraph(singleton(entryClass)).sort(excluded);
    }


    @Override
    public Collection<Class<? extends CodelistEntry>> getCodelistClasses() {
        return metadataMap.keySet();
    }


    @Override
    public Optional<Class<? extends CodelistEntry>> getEntryClass(String codelist) {
        return Optional.ofNullable(advancedCodelists.get(codelist));
    }


    @Override
    public void printMapping(String codelist, Class<? extends DataProvider> providerClass, PrintStream out) {
        Class<? extends CodelistEntry> entryClass = getEntryClass(codelist).orElse(CodelistEntry.class);
        EntryMetadata metadata = getEntryMetadata(entryClass);
        out.println("=================================================================");
        out.println("Codelist: " + codelist);
        out.println("Class: " + entryClass.getName());
        out.println("Provider: " + providerClass.getSimpleName());
        out.println("Provider codelist name: " + metadata.getProviderCodelistName(providerClass, codelist));
        out.println("Field mapping:");
        Map<Field, FieldMapping> fieldMap = metadata.getFieldMap(providerClass, codelist);
        fieldMap.forEach((k, v) -> out.println("  " + k.getName() + " -> " + v));
        Map<Field, EntryMetadata> embeddedMeta = metadata.getEmbedded();
        if (!embeddedMeta.isEmpty()) {
            out.println("Embedded mapping:");
            embeddedMeta.forEach((k, v) -> {
                out.println("  " + k.getName());
                v.getFieldMap(providerClass).forEach((mk, mv) -> out.println("    " + mk.getName() + " -> " + mv));
            });
        }
    }


    @Override
    public void printMapping(Class<? extends CodelistEntry> codelistClass, Class<? extends DataProvider> providerClass, PrintStream out) {
        EntryMetadata metadata = getEntryMetadata(codelistClass);
        out.println("=================================================================");
        out.println("Codelist: " + metadata.getCodelistName());
        out.println("Class: " + codelistClass.getName());
        out.println("Provider: " + providerClass.getSimpleName());
        out.println("Provider codelist name: " + metadata.getProviderCodelistName(providerClass, null));
        out.println("Field mapping:");
        Map<Field, FieldMapping> fieldMap = metadata.getFieldMap(providerClass);
        fieldMap.forEach((k, v) -> out.println("  " + k.getName() + " -> " + v));
        Map<Field, EntryMetadata> embeddedMeta = metadata.getEmbedded();
        if (!embeddedMeta.isEmpty()) {
            out.println("Embedded mapping:");
            embeddedMeta.forEach((k, v) -> {
                out.println("  " + k.getName());
                v.getFieldMap(providerClass).forEach((mk, mv) -> out.println("    " + mk.getName() + " -> " + mv));
            });
        }
    }


    @Override
    public String mappingToString(String codelist, Class<? extends DataProvider> providerClass) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            printMapping(codelist, providerClass, ps);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }


    @Override
    public String mappingToString(Class<? extends CodelistEntry> codelistClass, Class<? extends DataProvider> providerClass) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            printMapping(codelistClass, providerClass, ps);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }


    static CodelistEntry createShallowInstance(Class<? extends CodelistEntry> entryClass, String codeValue) {
        try {
            Constructor<? extends CodelistEntry> cr = entryClass.getDeclaredConstructor(String.class);
            cr.setAccessible(true);
            return cr.newInstance(codeValue);
        } catch (Exception e) {
            LOG.error("Can't create instance of CodelistEntry", e);
            throw new IllegalStateException(e);
        }
    }
}
