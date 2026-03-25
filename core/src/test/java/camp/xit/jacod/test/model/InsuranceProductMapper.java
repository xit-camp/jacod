package camp.xit.jacod.test.model;

import camp.xit.jacod.annotation.EntryFieldMapping;
import camp.xit.jacod.annotation.EntryMapping;
import camp.xit.jacod.model.InsuranceProduct;
import camp.xit.jacod.provider.csv.SimpleCsvDataProvider;

@EntryMapping(
    entryClass = InsuranceProduct.class,
    provider = SimpleCsvDataProvider.class,
    fields = { @EntryFieldMapping(
        field = "company",
        mappedField = "company",
        lookupRef = false) })
public class InsuranceProductMapper {
}
