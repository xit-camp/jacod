package camp.xit.jacoa.entry;

import camp.xit.jacoa.entry.QueryEntryGroup;
import camp.xit.jacoa.entry.EntryGroup;
import camp.xit.jacoa.CodelistClient;
import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.InsuranceProduct;
import camp.xit.jacoa.test.CodelistClientExtension;
import camp.xit.jacoa.test.CodelistClientExtension.CsvClient;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CodelistClientExtension.class)
public class QueryEntryGroupTest {

    @Test
    public void parse(@CsvClient CodelistClient client) throws Exception {
        EntryGroup group = new QueryEntryGroup(InsuranceProduct.class, "rate < 1.0 & company = \"02\"");
        Codelist<InsuranceProduct> filtered = group.getEntries(client.getCodelist(InsuranceProduct.class));
        assertThat(filtered.size(), is(2));
    }


    @Test
    public void nullValues(@CsvClient CodelistClient client) throws Exception {
        EntryGroup group = new QueryEntryGroup(InsuranceProduct.class, "order > 10");
        Codelist<InsuranceProduct> filtered = group.getEntries(client.getCodelist(InsuranceProduct.class));
        assertThat(filtered.size(), greaterThan(0));
    }
}
