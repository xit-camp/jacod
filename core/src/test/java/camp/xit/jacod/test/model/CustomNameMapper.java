
package camp.xit.jacod.test.model;

import camp.xit.jacod.BaseEntryMapping;
import camp.xit.jacod.provider.csv.SimpleCsvDataProvider;

@BaseEntryMapping(codelist = "CustomName", provider = SimpleCsvDataProvider.class, resourceName = "CSV_CUSTOM_NAME")
public class CustomNameMapper {
}
