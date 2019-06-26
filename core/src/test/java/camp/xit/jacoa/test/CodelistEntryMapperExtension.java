package camp.xit.jacoa.test;

import camp.xit.jacoa.impl.CodelistEntryMapper;
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
import static camp.xit.jacoa.CodelistClient.Builder.BASE_PACKAGE;

public class CodelistEntryMapperExtension implements ParameterResolver {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface FullMapper {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface BaseMapper {
    }


    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.isAnnotated(FullMapper.class)
                | parameterContext.isAnnotated(BaseMapper.class);
    }


    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return getCodelistEntryMapper(parameterContext.getParameter(), extensionContext);
    }


    private Object getCodelistEntryMapper(Parameter parameter, ExtensionContext extensionContext) {
        Class<?> type = parameter.getType();
        CodelistEntryMapper mapper = null;

        if (parameter.isAnnotationPresent(FullMapper.class)) {
            mapper = extensionContext.getRoot().getStore(Namespace.GLOBAL)//
                    .getOrComputeIfAbsent("FullMapper", key -> new CodelistEntryMapper(), CodelistEntryMapper.class);
        } else if (parameter.isAnnotationPresent(BaseMapper.class)) {
            mapper = extensionContext.getRoot().getStore(Namespace.GLOBAL)//
                    .getOrComputeIfAbsent("BaseMapper", key -> new CodelistEntryMapper(BASE_PACKAGE), CodelistEntryMapper.class);
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
