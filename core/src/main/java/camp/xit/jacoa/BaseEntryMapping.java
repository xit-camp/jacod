package camp.xit.jacoa;

import camp.xit.jacoa.provider.DataProvider;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author hlavki
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseEntryMapping {

    String codelist();


    String resourceName() default "";


    Class<? extends DataProvider> provider();


    EntryFieldMapping[] fields() default {};
}
