package camp.xit.jacod.cache2k;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.CodelistNotFoundException;
import camp.xit.jacod.cache2k.model.InsuranceProduct;
import camp.xit.jacod.cache2k.test.CsvErrorDataProvider;
import camp.xit.jacod.model.Codelist;
import java.time.Duration;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class SourceSystemDownTest {

    private static final long WAIT_TIME = 2;


    @Test
    public void simulatePartialSystemOutage() {
        try {
            CsvErrorDataProvider dp = new CsvErrorDataProvider();
            CodelistClient cl = new Cache2kCodelistClient.Builder()
                    .withDataProvider(dp)
                    .addScanPackages(InsuranceProduct.class.getPackageName())
                    .withExpiryTime(Duration.ofSeconds(WAIT_TIME)).build();

            Codelist<InsuranceProduct> ip = cl.getCodelist(InsuranceProduct.class);
            assertNotNull(ip);
            dp.setDown();
            ip = cl.getCodelist(InsuranceProduct.class);
            assertNotNull(ip);
            Thread.sleep(WAIT_TIME * 2 * 1000);
            ip = cl.getCodelist(InsuranceProduct.class);
            assertNotNull(ip);
            dp.setUp();
            Thread.sleep(WAIT_TIME * 2 * 1000);
            ip = cl.getCodelist(InsuranceProduct.class);
            assertNotNull(ip);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not fail!", e);
        }
    }


    @Test
    public void simulateFatalSystemOutage() {
        try {
            CsvErrorDataProvider dp = new CsvErrorDataProvider();
            CodelistClient cl = new Cache2kCodelistClient.Builder()
                    .addScanPackages(InsuranceProduct.class.getPackageName())
                    .withPrefetched() //empty
                    .withDataProvider(dp).withExpiryTime(Duration.ofSeconds(WAIT_TIME)).build();
            dp.setDown();
            Codelist<InsuranceProduct> ip = cl.getCodelist(InsuranceProduct.class);
            fail("Should not be here!");
        } catch (CodelistNotFoundException e) {
            assertThat(e.getCodelist(), is(InsuranceProduct.class.getSimpleName()));
        } catch (Exception e) {
            fail("Shoul not happened!", e);
        }
    }
}
