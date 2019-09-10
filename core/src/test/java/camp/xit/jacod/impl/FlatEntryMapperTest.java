package camp.xit.jacod.impl;

import camp.xit.jacod.provider.DataProvider;
import camp.xit.jacod.provider.EntryData;
import camp.xit.jacod.test.CodelistClientExtension;
import camp.xit.jacod.test.CodelistClientExtension.CsvDP;
import camp.xit.jacod.test.CodelistEntryMapperExtension;
import camp.xit.jacod.test.CodelistEntryMapperExtension.BaseMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CodelistEntryMapperExtension.class)
@ExtendWith(CodelistClientExtension.class)
public class FlatEntryMapperTest {

    @Test
    void basic(@BaseMapper CodelistEntryMapper mapper, @CsvDP DataProvider crafterDataProvider) throws Exception {
        FlatEntryMapper flatMapper = mapper.getFlatEntryMapper();
        Optional<List<EntryData>> data = crafterDataProvider.readEntries("BusinessPlace", -1);
        Class<? extends DataProvider> providerCl = crafterDataProvider.getClass();

        assertTrue(data.isPresent());
        List<EntryData> entriesData = data.get();
        assertFalse(entriesData.isEmpty());

        Map<String, Object> flatData = flatMapper.mapEntryToFlat("BusinessPlace", providerCl, entriesData.get(0));
        assertThat(flatData.size(), greaterThan(0));
        assertThat(flatData, not(hasKey("allowedBonusesBonusType")));
        assertThat(flatData, not(hasKey("allowedBonusesValidTo")));
        assertThat(flatData, not(hasKey("allowedBonusesValidFrom")));
    }
}
