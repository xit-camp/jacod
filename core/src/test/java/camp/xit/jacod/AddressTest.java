package camp.xit.jacod;

import camp.xit.jacod.model.BusinessPlace;
import camp.xit.jacod.test.CodelistClientExtension;
import camp.xit.jacod.test.CodelistClientExtension.CsvClient;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CodelistClientExtension.class)
public class AddressTest {

    @Test
    @Disabled
    public void displayValue(@CsvClient CodelistClient client) {
        BusinessPlace bp = client.getEntry(BusinessPlace.class, "A001");
        assertNotNull(bp);
        assertNotNull(bp.getBusinessAddress());
        assertThat(bp.getBusinessAddress().getDisplayValue(), is("Tuhovská 15 Bratislava"));
        assertThat(bp.getBusinessAddress().getPostalValue(), is("Tuhovská 15, Bratislava 83006"));
    }
}
