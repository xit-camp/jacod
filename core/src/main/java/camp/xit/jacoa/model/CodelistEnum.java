package camp.xit.jacoa.model;

import java.lang.reflect.ParameterizedType;

public interface CodelistEnum<T extends CodelistEntry> {

    default Class<T> getCodelistClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }
}
