package camp.xit.jacod;

import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.model.InsuranceProduct;
import camp.xit.jacod.test.CodelistClientExtension;
import camp.xit.jacod.test.CodelistClientExtension.CsvClient;
import camp.xit.jacod.test.CodelistClientExtension.ShallowCsvClient;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CodelistClientExtension.class)
class CodelistClientTest {

    private static final int CACHE_ITERATIONS = 10;


    @Test
    void basic(@CsvClient CodelistClient client) {
        Codelist<?> title = client.getCodelist("Title");
        assertThat(title.stream(true).filter(e -> e.getCode().contains("Dr")).count(), is(25L));
    }


    @Test
    void advancedWithFullReference(@CsvClient CodelistClient client) {
        Codelist<InsuranceProduct> cdl = client.getCodelist(InsuranceProduct.class);
        assertThat(cdl.stream(true).filter(e -> e.getType() != null).count(), is(6L));
        InsuranceProduct entry = cdl.getEntry(InsuranceProducts.A_02);
        assertNotNull(entry.getCompany());
        validateEntry(entry.getCompany());
        assertThat(entry.getCompany().getCode(), is("02"));
    }


    @Test
    void advancedWithShallowReference(@ShallowCsvClient CodelistClient client) {
        Codelist<InsuranceProduct> cdl = client.getCodelist(InsuranceProduct.class);
        assertThat(cdl.stream(true).filter(e -> e.getType() != null).count(), is(6L));
        InsuranceProduct entry = cdl.getEntry(InsuranceProducts.A_02);
        assertNotNull(entry.getCompany());
        assertThat(entry.getCompany().getCode(), is("02"));
        assertNull(entry.getCompany().getName());
        assertNull(entry.getCompany().getOrder());
        assertNull(entry.getCompany().getValidFrom());
        assertNull(entry.getCompany().getValidTo());
        assertNull(entry.getCompany().getSelected());
    }


    @Test
    void referenceNotFound(@CsvClient CodelistClient client) {
        try {
            Codelist<InsuranceProduct> cdl = client.getCodelist(InsuranceProduct.class);
            assertThat(cdl.stream(true).filter(e -> e.getType() != null).count(), is(6L));
            assertNotNull(cdl.getEntry("C_03").getCompany());
            fail("Should not get here!");
        } catch (EntryNotFoundException e) {
            assertThat(e.getCode(), is("C_03"));
        }
    }


    @Test
    void codelistNotFound(@CsvClient CodelistClient client) {
        try {
            client.getCodelist("NonExist");
            fail("Should not get here!");
        } catch (CodelistNotFoundException e) {
            assertThat(e.getCodelist(), is("NonExist"));
        }
    }


    @Test
    void emptyValue(@CsvClient CodelistClient client) {
        try {
            Codelist cdl = client.getCodelist("ContractStateWrong");
            fail("Should throw exception!");
        } catch (RuntimeException e) {
        }
    }


    @Test
    @Disabled("Non deterministic")
    void cache(@CsvClient CodelistClient client) {

        List<Long> times = new ArrayList<>();
        for (int i = 0; i < CACHE_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            client.getCodelist("ProductType");
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        double max = times.get(0) / 1.5;
        for (int i = 1; i < CACHE_ITERATIONS; i++) {
            assertTrue(times.get(i) < max);
        }
    }


    @Test
    void reload(@CsvClient CodelistClient client) {
        Codelist<InsuranceProduct> promotions = client.getCodelist(InsuranceProduct.class);
        assertThat(promotions.size(), is(6));
        client.reloadCache(InsuranceProduct.class.getSimpleName());
        assertThat(promotions.size(), is(6));
    }


    @Test
    void booleanQuery(@CsvClient CodelistClient client) {
        Codelist<InsuranceProduct> promotions = client.getFilteredCodelist(InsuranceProduct.class, "company.selected = TRUE");
        assertThat(promotions.size(), is(2));
    }


    @Test
    void regexpQuery(@CsvClient CodelistClient client) {
        Codelist<InsuranceProduct> promotions = client.getFilteredCodelist(InsuranceProduct.class, "description ~= \"^Basic.*$\"");
        assertThat(promotions.size(), is(3));
    }


    private void validateEntry(CodelistEntry entry) {
        assertNotNull(entry.getCode());
        assertNotNull(entry.getName());
        assertNotNull(entry.getValidFrom());
        assertNotNull(entry.getValidTo());
        assertNotNull(entry.getOrder());
    }
}
