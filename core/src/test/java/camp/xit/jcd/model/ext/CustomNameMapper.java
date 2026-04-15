package camp.xit.jcd.model.ext;

import camp.xit.jacod.annotation.BaseEntryMapping;
import camp.xit.jacod.provider.csv.SimpleCsvDataProvider;

@BaseEntryMapping(codelist = "CustomName", provider = SimpleCsvDataProvider.class, resourceName = "CSV_CUSTOM_NAME")
public class CustomNameMapper {
}
