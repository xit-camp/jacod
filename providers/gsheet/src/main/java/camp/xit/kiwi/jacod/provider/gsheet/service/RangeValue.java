package camp.xit.kiwi.jacod.provider.gsheet.service;

import com.fasterxml.jackson.databind.JsonNode;

public class RangeValue {

    private String range;
    private String majorDimension;
    private JsonNode values;


    public String getRange() {
        return range;
    }


    public void setRange(String range) {
        this.range = range;
    }


    public String getMajorDimension() {
        return majorDimension;
    }


    public void setMajorDimension(String majorDimension) {
        this.majorDimension = majorDimension;
    }


    public JsonNode getValues() {
        return values;
    }


    public void setValues(JsonNode values) {
        this.values = values;
    }

}
