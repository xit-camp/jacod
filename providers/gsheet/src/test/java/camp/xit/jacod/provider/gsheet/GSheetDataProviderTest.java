package camp.xit.jacod.provider.gsheet;

import static camp.xit.jacod.provider.gsheet.JsonDataReader.readResourceAs;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import camp.xit.jacod.CodelistClient;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.provider.EntryData;
import camp.xit.jacod.provider.gsheet.service.GSheetService;
import camp.xit.jacod.provider.gsheet.service.RangeValue;

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
