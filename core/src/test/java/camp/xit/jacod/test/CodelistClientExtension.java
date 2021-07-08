package camp.xit.jacod.test;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.BonusType;
import camp.xit.jacod.model.BusinessPlace;
import camp.xit.jacod.model.ContractState;
import camp.xit.jacod.model.InsuranceProduct;
import camp.xit.jacod.model.PaymentDeferment;
import camp.xit.jacod.model.Title;
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
                        .getOrComputeIfAbsent("CsvClient", key -> getCsvClient(false), CodelistClient.class);
            } else if (parameter.isAnnotationPresent(ShallowCsvClient.class)) {
                result = extensionContext.getRoot().getStore(Namespace.GLOBAL)//
                        .getOrComputeIfAbsent("ShallowCsvClient", key -> getCsvClient(true), CodelistClient.class);
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


    private CodelistClient getCsvClient(boolean shallow) {
        DataProvider provider = new SimpleCsvDataProvider();
        CodelistClient.Builder builder = new CodelistClient.Builder()
                .withDataProvider(provider)
                .disableMappers()
                .codelists(BonusType.class, BusinessPlace.class, ContractState.class, InsuranceProduct.class,
                        Title.class, PaymentDeferment.class);

        if (shallow) builder.shallowReferences();

        return builder.build();
    }
}
