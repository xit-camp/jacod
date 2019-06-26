
package camp.xit.jacoa.test.model;

import camp.xit.jacoa.BaseEntryMapping;
import camp.xit.jacoa.provider.csv.SimpleCsvDataProvider;

@BaseEntryMapping(codelist = "CustomName", provider = SimpleCsvDataProvider.class, resourceName = "CSV_CUSTOM_NAME")
public class CustomNameMapper {
}
