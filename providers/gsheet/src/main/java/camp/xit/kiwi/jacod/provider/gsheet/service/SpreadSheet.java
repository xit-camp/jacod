package camp.xit.kiwi.jacod.provider.gsheet.service;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpreadSheet {

    private String spreadsheetId;
    private Properties properties;
    private List<Sheet> sheets;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Properties {

        private String title;
        private String locale;
        private String autoRecalc;
        private String timeZone;
    }
}
