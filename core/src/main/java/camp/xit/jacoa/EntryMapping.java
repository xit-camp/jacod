package camp.xit.jacoa;

import camp.xit.jacoa.model.CodelistEntry;
import camp.xit.jacoa.provider.DataProvider;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EntryMappings.class)
public @interface EntryMapping {

    Class<? extends DataProvider> provider();


    String resourceName() default "";


    Class<?> entryClass() default CodelistEntry.class;


    boolean inheritParent() default true;


    EntryFieldMapping[] fields() default {};
}
