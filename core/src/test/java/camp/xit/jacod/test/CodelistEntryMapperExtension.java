package camp.xit.jacod.test;

import camp.xit.jacod.impl.CodelistEntryMapper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class CodelistEntryMapperExtension implements ParameterResolver {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface EntryMapper {
    }


    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.isAnnotated(EntryMapper.class);
    }


    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return getCodelistEntryMapper(parameterContext.getParameter(), extensionContext);
    }


    private Object getCodelistEntryMapper(Parameter parameter, ExtensionContext extensionContext) {
        Class<?> type = parameter.getType();
        CodelistEntryMapper mapper = null;

        if (parameter.isAnnotationPresent(EntryMapper.class)) {
            mapper = extensionContext.getRoot().getStore(Namespace.GLOBAL)//
                    .getOrComputeIfAbsent("BaseMapper", key -> new CodelistEntryMapper(), CodelistEntryMapper.class);
        }
        if (mapper == null) {
            throw new ParameterResolutionException("Parameter without annotation! Use @DefaultMapper or @BaseMapper annotation");
        }
        if (CodelistEntryMapper.class.equals(type)) {
            return mapper;
        }
        throw new ParameterResolutionException("Cannot assing CodelistEntryMapper to " + type);
    }
}
