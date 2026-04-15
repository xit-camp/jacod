package camp.xit.jacod.cache2k.model;

import camp.xit.jacod.annotation.EntryFieldMapping;
import camp.xit.jacod.annotation.EntryMapping;
import camp.xit.jacod.cache2k.test.SimpleCsvDataProvider;

@EntryMapping(entryClass = InsuranceProduct.class, provider = SimpleCsvDataProvider.class, fields = {
    @EntryFieldMapping(field = "company", mappedField = "company", lookupRef = false)
})
public class InsuranceProductMapper {
}
