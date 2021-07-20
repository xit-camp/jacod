package camp.xit.jacod.test;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.csv.SimpleCsvDataProvider;
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

public class CodelistClientExtension implements ParameterResolver {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface FullScanCsvClient {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface CsvClient {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface ShallowCsvClient {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface CsvDP {
    }


    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.isAnnotated(CsvClient.class)
                | parameterContext.isAnnotated(FullScanCsvClient.class)
                | parameterContext.isAnnotated(ShallowCsvClient.class)
                | parameterContext.isAnnotated(CsvDP.class);
    }


    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return getCodelistClient(parameterContext.getParameter(), extensionContext);
    }


    private Object getCodelistClient(Parameter parameter, ExtensionContext extensionContext) {
        Class<?> type = parameter.getType();

        Object result = null;
        if (CodelistClient.class.isAssignableFrom(type)) {
            if (parameter.isAnnotationPresent(CsvClient.class)) {
                result = extensionContext.getRoot().getStore(Namespace.GLOBAL)//
                        .getOrComputeIfAbsent("CsvClient", key -> getCsvClient(), CodelistClient.class);
            } else if (parameter.isAnnotationPresent(FullScanCsvClient.class)) {
                result = extensionContext.getRoot().getStore(Namespace.GLOBAL)//
                        .getOrComputeIfAbsent("FullScanCsvClient", key -> getFullScanCsvClient(), CodelistClient.class);
            } else if (parameter.isAnnotationPresent(ShallowCsvClient.class)) {
                result = extensionContext.getRoot().getStore(Namespace.GLOBAL)//
                        .getOrComputeIfAbsent("ShallowCsvClient", key -> getShallowCsvClient(), CodelistClient.class);
            }
        } else if (DataProvider.class.isAssignableFrom(type)) {
            if (parameter.isAnnotationPresent(CsvDP.class)) {
                result = extensionContext.getRoot().getStore(Namespace.GLOBAL)//
                        .getOrComputeIfAbsent("CsvDP", key -> new SimpleCsvDataProvider(), DataProvider.class);
            }
        }

        if (result == null) {
            String msg = "Parameter " + parameter.getName() + " has invalid extension annotation";
            throw new ParameterResolutionException(msg);
        }
        return result;
    }


    private CodelistClient getCsvClient() {
        return getCodelistClientBuilder().build();
    }


    private CodelistClient getFullScanCsvClient() {
        return getCodelistClientBuilder().scanFullClasspath().build();
    }


    private CodelistClient getShallowCsvClient() {
        return getCodelistClientBuilder().scanFullClasspath().shallowReferences().build();
    }


    private CodelistClient.Builder getCodelistClientBuilder() {
        return new CodelistClient.Builder()
                .withDataProvider(new SimpleCsvDataProvider());
    }
}
