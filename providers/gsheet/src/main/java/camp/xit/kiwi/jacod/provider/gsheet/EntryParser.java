package camp.xit.kiwi.jacod.provider.gsheet;

import camp.xit.jacod.provider.EntryData;
import camp.xit.kiwi.jacod.provider.gsheet.service.RangeValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;
import static java.util.Optional.ofNullable;

class EntryParser {

    private EntryParser() {
    }


    static final List<EntryData> parse(RangeValue sheetValues) {
        JsonNode valuesNode = sheetValues.getValues();
        ArrayNode fieldNamesNode = null;
        List<EntryData> result = new ArrayList<>();
        if (valuesNode.isArray()) {
            int rowNum = 0;
            for (JsonNode rowNode : valuesNode) {
                if (rowNode.isArray() && rowNum == 0) {
                    fieldNamesNode = (ArrayNode) rowNode;
                } else if (rowNode.isArray()) {
                    ArrayNode arrRowNode = (ArrayNode) rowNode;
                    EntryData data = new EntryData();
                    for (int idx = 0; idx < fieldNamesNode.size(); idx++) {
                        String key = fieldNamesNode.get(idx).asText();
                        String value = ofNullable(arrRowNode.get(idx)).map(v -> v.asText()).orElse(null);
                        if (value != null && value.isEmpty()) value = null;
                        data.addField(key, value);
                    }
                    result.add(data);
                }
                rowNum++;
            }
        }
        return result;
    }
}
