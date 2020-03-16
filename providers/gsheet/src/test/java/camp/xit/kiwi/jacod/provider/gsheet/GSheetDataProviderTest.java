package camp.xit.kiwi.jacod.provider.gsheet;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.EntryData;
import static camp.xit.kiwi.jacod.provider.gsheet.JsonDataReader.readResourceAs;
import camp.xit.kiwi.jacod.provider.gsheet.service.GSheetService;
import camp.xit.kiwi.jacod.provider.gsheet.service.RangeValue;
import java.util.List;
import java.util.Optional;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GSheetDataProviderTest {

    @Mock
    GSheetService gsheetService;


    @Test
    public void readData() {
        GSheetDataProvider provider = new GSheetDataProvider("GoogleMock", gsheetService, "");
        when(gsheetService.getSheetValues("", "Country")).thenReturn(readResourceAs("/data/Country.json", RangeValue.class));
        Optional<List<EntryData>> data = provider.readEntries("Country", -1);
        assertThat(data, is(not(empty())));
        assertThat(data.get().size(), greaterThan(100));
    }


    @Test
    public void codelist() {
        GSheetDataProvider provider = new GSheetDataProvider("GoogleMock", gsheetService, "");
        CodelistClient client = new CodelistClient.Builder<>().withDataProvider(provider).noPrefetched().build();
        when(gsheetService.getSheetValues("", "Country")).thenReturn(readResourceAs("/data/Country.json", RangeValue.class));
        Codelist<? extends CodelistEntry> countries = client.getCodelist("Country");
        assertNotNull(countries);
        CodelistEntry slovakia = countries.get("SK");
        assertNotNull(slovakia);
        assertThat(slovakia.getName(), is("Slovenská republika"));
    }
}
