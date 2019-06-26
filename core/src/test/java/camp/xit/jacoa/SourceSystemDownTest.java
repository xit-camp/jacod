package camp.xit.jacoa;

import camp.xit.jacoa.CodelistClient;
import camp.xit.jacoa.CodelistNotFoundException;
import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.InsuranceProduct;
import camp.xit.jacoa.provider.csv.CsvErrorDataProvider;
import java.util.concurrent.TimeUnit;
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
            CodelistClient cl = new CodelistClient.Builder()
                    .withDataProvider(dp)
                    .withExpiryTime(WAIT_TIME, TimeUnit.SECONDS).build();

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
            CodelistClient cl = new CodelistClient.Builder()
                    .withPrefetched()
                    .withDataProvider(dp).withExpiryTime(WAIT_TIME, TimeUnit.SECONDS).build();
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
