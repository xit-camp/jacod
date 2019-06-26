package camp.xit.jacod.entry;

import camp.xit.jacod.entry.QueryEntryGroup;
import camp.xit.jacod.entry.EntryGroup;
import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.InsuranceProduct;
import camp.xit.jacod.test.CodelistClientExtension;
import camp.xit.jacod.test.CodelistClientExtension.CsvClient;
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
