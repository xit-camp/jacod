package camp.xit.jcd.model.ext;

import camp.xit.jacod.annotation.EntryFieldMapping;
import camp.xit.jacod.annotation.EntryMapping;
import camp.xit.jacod.provider.csv.SimpleCsvDataProvider;
import camp.xit.jcd.model.InsuranceProduct;

@EntryMapping(
    entryClass = InsuranceProduct.class,
    provider = SimpleCsvDataProvider.class,
    fields = {
        @EntryFieldMapping(field = "company", mappedField = "company") // , lookupRef = false
    }
)
public class InsuranceProductMapper {
}
