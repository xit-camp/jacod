package camp.xit.jacod;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

public class ExpirationSupplierTest {

    private static final int WAIT_TIME = 2;


    @Test
    public void testReadChange() throws Exception {
        Supplier<Long> sup = ExpirationSupplier.of((v, m) -> System.currentTimeMillis(), 1, TimeUnit.SECONDS);
        Long value1 = sup.get();
        Long value2 = sup.get();
        System.out.println("Waiting for " + WAIT_TIME + " seconds");
        Thread.sleep(WAIT_TIME * 1000);
        Long value3 = sup.get();
        assertThat(value1, is(value2));
        assertThat(value1, not(value3));
    }
}
