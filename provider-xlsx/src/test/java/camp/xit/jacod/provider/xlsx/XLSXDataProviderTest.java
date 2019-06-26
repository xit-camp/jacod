package camp.xit.jacod.provider.xlsx;

import camp.xit.jacod.provider.xlsx.XLSXDataProvider;
import camp.xit.jacod.model.InsuranceProduct;
import camp.xit.jacod.model.Title;
import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.EntryData;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author hlavki
 */
public class XLSXDataProviderTest {

    @Test
    public void agreementType() throws Exception {
        XLSXDataProvider p = getProvider();
        Optional<List<EntryData>> entries = p.readEntries("RecordType", -1);
        assertTrue(entries.isPresent());
        assertThat(entries.get().size(), greaterThan(0));
    }


    @Test
    public void title() throws Exception {
        XLSXDataProvider dp = getProvider();
        CodelistClient cl = new CodelistClient.Builder().withDataProvider(dp).build();
        Codelist<Title> sc = cl.getCodelist(Title.class);
        assertThat(sc.size(), is(52));
        assertTrue(Title.class.isAssignableFrom(sc.getEntry("PHD").getClass()));
        Title phd = sc.getEntry("PHD");
        assertTrue(Title.class.isAssignableFrom(phd.getClass()));
        assertThat(phd.getPosition(), is(Title.Position.AFTER));

        CodelistEntry phdEntry = cl.getEntry("Title", "PHD");
        assertTrue(Title.class.isAssignableFrom(phdEntry.getClass()));
    }


    @Test
    public void insuranceProduct() throws Exception {
        XLSXDataProvider dp = getProvider();
        CodelistClient cl = new CodelistClient.Builder().withDataProvider(dp).build();
        Codelist<InsuranceProduct> sc = cl.getCodelist(InsuranceProduct.class);
        assertThat(sc.size(), is(6));
        assertTrue(InsuranceProduct.class.isAssignableFrom(sc.getEntry("01_A").getClass()));
        InsuranceProduct a = sc.getEntry("01_A");
        assertTrue(InsuranceProduct.class.isAssignableFrom(a.getClass()));
        assertThat(a.getType(), is(InsuranceProduct.Type.A));
    }


    private XLSXDataProvider getProvider() {
        return new XLSXDataProvider("/codelists.xlsx");
    }
}
