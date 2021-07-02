package camp.xit.kiwi.jacod.provider.gsheet.service;

import java.util.List;

public class ValueRanges {

    private String spreadsheetId;
    private List<RangeValue> valueRanges;


    public ValueRanges() {
    }


    public String getSpreadsheetId() {
        return spreadsheetId;
    }


    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }


    public List<RangeValue> getValueRanges() {
        return valueRanges;
    }


    public void setValueRanges(List<RangeValue> valueRanges) {
        this.valueRanges = valueRanges;
    }
}
