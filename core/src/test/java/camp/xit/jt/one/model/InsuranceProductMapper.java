package camp.xit.jt.one.model;

import camp.xit.jacod.annotation.EntryFieldMapping;
import camp.xit.jacod.annotation.EntryMapping;
import camp.xit.jacod.provider.csv.SimpleCsvDataProvider;
import camp.xit.jt.model.InsuranceProduct;

@EntryMapping(
    entryClass = InsuranceProduct.class,
    provider = SimpleCsvDataProvider.class,
    fields = { @EntryFieldMapping(
        field = "company",
        mappedField = "company",
        lookupRef = false) })
public class InsuranceProductMapper {
}
