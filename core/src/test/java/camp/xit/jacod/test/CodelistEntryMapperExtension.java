package camp.xit.jacod.test;

import camp.xit.jacod.impl.CodelistEntryMapper;
import camp.xit.jacod.impl.MappersReg;
import camp.xit.jacod.model.BonusType;
import camp.xit.jacod.model.BusinessPlace;
import camp.xit.jacod.model.ContractState;
import camp.xit.jacod.model.InsuranceProduct;
import camp.xit.jacod.model.PaymentDeferment;
import camp.xit.jacod.model.Title;
import camp.xit.jacod.test.model.CustomNameMapper;
import camp.xit.jacod.test.model.InsuranceProductMapper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.Set;
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
                    .getOrComputeIfAbsent("BaseMapper", key -> new CodelistEntryMapper(getMappersReg()), CodelistEntryMapper.class);
        }
        if (mapper == null) {
            throw new ParameterResolutionException("Parameter without annotation! Use @DefaultMapper or @BaseMapper annotation");
        }
        if (CodelistEntryMapper.class.equals(type)) {
            return mapper;
        }
        throw new ParameterResolutionException("Cannot assing CodelistEntryMapper to " + type);
    }


    private MappersReg getMappersReg() {
        var codelistMapping = MappersReg.mappingFromClasses(BonusType.class, BusinessPlace.class,
                ContractState.class, InsuranceProduct.class, Title.class, PaymentDeferment.class);

        return new MappersReg(codelistMapping, Set.of(InsuranceProductMapper.class, CustomNameMapper.class));
    }
}
