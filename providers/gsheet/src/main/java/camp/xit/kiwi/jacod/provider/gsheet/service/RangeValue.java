package camp.xit.kiwi.jacod.provider.gsheet.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RangeValue {

    private String range;
    private String majorDimension;
    private JsonNode values;
}
