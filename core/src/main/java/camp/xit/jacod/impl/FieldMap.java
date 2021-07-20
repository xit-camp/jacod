package camp.xit.jacod.impl;

import camp.xit.jacod.EntryFieldMapping;
import camp.xit.jacod.EntryMapping;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class FieldMap<T> {

    private final String customName;
    private final Map<T, FieldMapping> fields;
    private final boolean inheritParent;


    public FieldMap(EntryMapping mapping, Map<T, FieldMapping> fields) {
        this(mapping.resourceName(), fields, mapping.inheritParent());
    }


    public FieldMap(String customName, Map<T, FieldMapping> fields, boolean inheritParent) {
        this.customName = customName;
        this.fields = fields;
        this.inheritParent = inheritParent;
    }


    public FieldMap(FieldMap fm) {
        this.customName = fm.customName;
        this.fields = new HashMap<>();
        this.inheritParent = fm.inheritParent;
    }


    public Optional<String> getCustomName() {
        return !customName.isEmpty() ? Optional.of(customName) : Optional.empty();
    }


    public Map<T, FieldMapping> getFields() {
        return fields;
    }


    public boolean isInheritParent() {
        return inheritParent;
    }


    @Override
    public String toString() {
        return String.valueOf(fields);
    }

    static class FieldMapping {

        private final String mappedField;
        private final boolean lookupRef;


        public FieldMapping(EntryFieldMapping ann) {
            this(ann.mappedField(), ann.lookupRef());
        }


        public FieldMapping(String mappedField) {
            this(mappedField, true);
        }


        public FieldMapping(String mappedField, boolean lookupRef) {
            this.mappedField = mappedField;
            this.lookupRef = lookupRef;
        }


        public FieldMapping addPrefix(String prefix) {
            return new FieldMapping(addPrefix(prefix, mappedField), lookupRef);
        }


        private String addPrefix(String prefix, String value) {
            return prefix + value.substring(0, 1).toUpperCase() + value.substring(1);
        }


        public String getMappedField() {
            return mappedField;
        }


        public boolean isLookupRef() {
            return lookupRef;
        }


        @Override
        public String toString() {
            return "FieldMapping{" + "mappedField=" + mappedField + ", lookupRef=" + lookupRef + '}';
        }
    }
}
