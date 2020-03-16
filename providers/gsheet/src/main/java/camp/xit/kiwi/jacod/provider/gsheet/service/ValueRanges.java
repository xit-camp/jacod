package camp.xit.kiwi.jacod.provider.gsheet.service;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ValueRanges {

    private String spreadsheetId;
    private List<RangeValue> valueRanges;
}
