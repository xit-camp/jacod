package camp.xit.jacoa.impl;

import camp.xit.jacoa.EntryFieldMapping;
import camp.xit.jacoa.EntryMapping;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.ToString;

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

    @Getter
    @ToString
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
    }
}
