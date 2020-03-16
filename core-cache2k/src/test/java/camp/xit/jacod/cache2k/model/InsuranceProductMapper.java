package camp.xit.jacod.cache2k.model;

import camp.xit.jacod.EntryFieldMapping;
import camp.xit.jacod.EntryMapping;
import camp.xit.jacod.cache2k.test.SimpleCsvDataProvider;

@EntryMapping(entryClass = InsuranceProduct.class, provider = SimpleCsvDataProvider.class, fields = {
    @EntryFieldMapping(field = "company", mappedField = "company", lookupRef = false)
})
public class InsuranceProductMapper {
}
