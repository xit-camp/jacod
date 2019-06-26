package camp.xit.jacod;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.test.CodelistClientExtension;
import camp.xit.jacod.test.CodelistClientExtension.FullScanCsvClient;
import camp.xit.jacod.test.model.Brand;
import camp.xit.jacod.test.model.CommunicationChannel;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({CodelistClientExtension.class})
public class CustomCodelistTest {

    private static final int CACHE_ITERATIONS = 10;


    @Test
    void customBonusType(@FullScanCsvClient CodelistClient client) {
        Codelist<Brand> title = client.getCodelist(Brand.class);
        assertThat(title.size(), is(1));
        assertThat(title.stream(true).filter(e -> e.getCode().equals("GENIUS")).count(), is(1L));
    }


    @Test
    void enumEquals(@FullScanCsvClient CodelistClient client) {
        Codelist<CommunicationChannel> channels = client.getCodelist(CommunicationChannel.class);
        CommunicationChannel email = channels.getEntry("EMAIL");
        assertTrue(email.equals(CommunicationChannel.Types.EMAIL));
        assertFalse(email.equals(CommunicationChannel.Types.LETTER));
    }


    @Test
    @Disabled("Non deterministic")
    void cache(@FullScanCsvClient CodelistClient client) {

        List<Long> times = new ArrayList<>();
        for (int i = 0; i < CACHE_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            client.getCodelist(Brand.class);
            long end = System.currentTimeMillis();
            times.add(end - start);
        }
        double max = times.get(0) / 2;
        for (int i = 1; i < CACHE_ITERATIONS; i++) {
            assertTrue(times.get(i) < max);
        }
    }
}
