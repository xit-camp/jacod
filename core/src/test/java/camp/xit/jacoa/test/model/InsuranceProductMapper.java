
package camp.xit.jacoa.test.model;

import camp.xit.jacoa.EntryFieldMapping;
import camp.xit.jacoa.EntryMapping;
import camp.xit.jacoa.model.InsuranceProduct;
import camp.xit.jacoa.provider.csv.SimpleCsvDataProvider;

@EntryMapping(entryClass = InsuranceProduct.class, provider = SimpleCsvDataProvider.class, fields = {
    @EntryFieldMapping(field = "company", mappedField = "company", lookupRef = false)
})
public class InsuranceProductMapper {
}
