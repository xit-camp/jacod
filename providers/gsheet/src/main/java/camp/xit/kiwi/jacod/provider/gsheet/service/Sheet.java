package camp.xit.kiwi.jacod.provider.gsheet.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Sheet {

    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Properties {

        private Integer id;
        private String title;
        private Integer index;
        @JsonProperty("sheetType")
        private String type;
    }
}
