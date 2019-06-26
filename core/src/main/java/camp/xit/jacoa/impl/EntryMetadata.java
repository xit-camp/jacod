package camp.xit.jacoa.impl;

import camp.xit.jacoa.Embeddable;
import camp.xit.jacoa.EntryRef;
import camp.xit.jacoa.model.CodelistEntry;
import camp.xit.jacoa.provider.DataProvider;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class EntryMetadata {

    protected final Class<?> entryClass;
    protected final Map<Field, FieldMap.FieldMapping> fieldMap;
    protected final Map<Class<? extends DataProvider>, FieldMap<Field>> providerMappedFields;
    protected final Set<Field> refs;
    protected final Map<Field, EntryMetadata> embedded;
    protected final BaseEntryMetadata baseMetadata;


    public EntryMetadata(Class<?> entryClass, Map<Field, FieldMap.FieldMapping> fieldMap,
            Map<Class<? extends DataProvider>, FieldMap<Field>> providerMappedFields,
            BaseEntryMetadata baseMetadata, Set<Field> refs, Map<Field, EntryMetadata> embedded) {
        this.entryClass = entryClass;
        this.fieldMap = fieldMap;
        this.providerMappedFields = providerMappedFields;
        this.refs = refs;
        this.embedded = embedded;
        this.baseMetadata = baseMetadata;
    }


    public Map<Field, FieldMap.FieldMapping> getFieldMap(Class<? extends DataProvider> providerClass) {
        Map<Field, FieldMap.FieldMapping> result = new HashMap<>();

        FieldMap<Field> providerMapping = providerMappedFields.get(providerClass);

        if (baseMetadata != null && (providerMapping == null || providerMapping.isInheritParent())) {
            result.putAll(baseMetadata.getFieldMap(providerClass));
        }
        result.putAll(providerMapping != null ? providerMapping.getFields() : fieldMap);
        return result;
    }


    public Map<Field, FieldMap.FieldMapping> getFieldMap(Class<? extends DataProvider> providerClass, String codelist) {
        return getFieldMap(providerClass);
    }


    public Set<Field> getReferencies() {
        return refs;
    }


    public String getReferenceName(Field field) {
        Class<?> refType = getReferenceType(field);
        if (isCollection(field)) {
            EntryRef eref = field.getAnnotation(EntryRef.class);
            boolean advancedType = !refType.equals(CodelistEntry.class)
                    && CodelistEntry.class.isAssignableFrom(refType);
            if (!advancedType && (eref == null || eref.value().isEmpty())) {
                throw new IllegalArgumentException("Specify @EntryRef annotation for " + field);
            }
            return advancedType ? refType.getSimpleName() : eref.value();
        } else {
            EntryRef eref = field.getAnnotation(EntryRef.class);
            boolean needRefName = refType.equals(CodelistEntry.class);
            if (needRefName && (eref == null || eref.value().isEmpty())) {
                throw new IllegalArgumentException("Specify @EntryRef annotation for " + getCodelistName() + "." + field.getName());
            }
            return needRefName ? eref.value() : refType.getSimpleName();
        }
    }


    public static Class<?> getReferenceType(Field field) {
        return isCollection(field) ? (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0] : field.getType();
    }


    public static boolean isCodelistReference(Field field) {
        return CodelistEntry.class.isAssignableFrom(getReferenceType(field));
    }


    public static boolean isCollection(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }


    public boolean isBaseCodelist() {
        return entryClass.equals(CodelistEntry.class);
    }


    public boolean hasReferences() {
        return refs.isEmpty();
    }


    public void addEmbedded(Field field, EntryMetadata metadata) {
        this.embedded.put(field, metadata);
    }


    public Map<Field, EntryMetadata> getEmbedded() {
        return embedded;
    }


    public EntryMetadata getEmbeddedFor(Field field) {
        return embedded.get(field);
    }


    public Class<?> getEntryClass() {
        return entryClass;
    }


    public String getCodelistName() {
        return getEntryClass().getSimpleName();
    }


    public Optional<String> getProviderCodelistName(Class<? extends DataProvider> providerClass, String name) {
        FieldMap<Field> fm = providerMappedFields.get(providerClass);
        return fm == null || !fm.getCustomName().isPresent()
                ? (isBaseCodelist() ? Optional.ofNullable(name) : Optional.empty())
                : fm.getCustomName();
    }


    public Collection<Field> getFields() {
        return fieldMap.keySet();
    }


    public boolean isEmbedded() {
        return entryClass.isAnnotationPresent(Embeddable.class);
    }


    @Override
    public String toString() {
        return fieldMap.toString();
    }
}
