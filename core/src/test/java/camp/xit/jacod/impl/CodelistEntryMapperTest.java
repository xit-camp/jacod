package camp.xit.jacod.impl;

import camp.xit.jacod.model.BusinessPlace;
import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.model.InsuranceProduct;
import camp.xit.jacod.provider.csv.SimpleCsvDataProvider;
import camp.xit.jacod.test.CodelistEntryMapperExtension;
import camp.xit.jacod.test.CodelistEntryMapperExtension.EntryMapper;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CodelistEntryMapperExtension.class)
class CodelistEntryMapperTest {

    @Test
    void basic(@EntryMapper CodelistEntryMapper mapper) {
        assertFalse(mapper.isAdvancedCodelist("InsuranceCompany"));
        assertTrue(mapper.isAdvancedCodelist("InsuranceProduct"));
        assertTrue(mapper.isAdvancedCodelist("Title"));
    }


    @Test
    void advanced(@EntryMapper CodelistEntryMapper mapper) {
        assertTrue(mapper.isAdvancedCodelist("BusinessPlace"));
        EntryMetadata metadata = mapper.getEntryMetadata(BusinessPlace.class);
        assertThat(metadata.getEmbedded().size(), is(2));
    }


    @Test
    void references(@EntryMapper CodelistEntryMapper mapper) {
        assertThat(mapper.getAllDependencies(CodelistEntry.class).size(), is(0));
        assertThat(mapper.getAllDependencies(InsuranceProduct.class).size(), is(4));
        assertThat(mapper.getCodelistDependencies(InsuranceProduct.class).size(), is(3));
    }


    @Test
    void dependencyGraph(@EntryMapper CodelistEntryMapper mapper) {
        assertThat(mapper.getSortedDependencies(List.of("BonusType")), contains("BonusType"));
        assertThat(mapper.getSortedDependencies(List.of("InsuranceProduct")),
                contains("InsuranceCompany", "InsuranceCalculationType", "IncomeSource", "InsuranceProduct"));
    }


    @Test
    void nameFromProvider(@EntryMapper CodelistEntryMapper mapper) {
        Map<String, String> mapping = mapper.getReverseProviderNames(SimpleCsvDataProvider.class);
        assertNotNull(mapping);
        assertTrue(mapping.containsKey("CSV_CUSTOM_NAME"));
        assertThat(mapping.get("CSV_CUSTOM_NAME"), is("CustomName"));
    }


    @Test
    void usagesOf(@EntryMapper CodelistEntryMapper mapper) {
        Collection<String> usagesOfBPC = mapper.getUsagesOf("InsuranceCompany");
        assertFalse(usagesOfBPC.isEmpty());
        assertThat(usagesOfBPC, containsInAnyOrder("InsuranceProduct"));


        Collection<String> usagesOfIS = mapper.getUsagesOf("IncomeSource");
        assertFalse(usagesOfIS.isEmpty());
        assertThat(usagesOfIS, containsInAnyOrder("InsuranceProduct"));
    }
}
