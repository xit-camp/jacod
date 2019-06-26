package camp.xit.jacod;

import camp.xit.jacod.provider.DataProvider;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseEntryMapping {

    String codelist();


    String resourceName() default "";


    Class<? extends DataProvider> provider();


    EntryFieldMapping[] fields() default {};
}
