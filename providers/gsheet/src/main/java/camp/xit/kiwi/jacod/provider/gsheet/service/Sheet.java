package camp.xit.kiwi.jacod.provider.gsheet.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sheet {

    private Properties properties;


    public Sheet() {
    }


    public Properties getProperties() {
        return properties;
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static class Properties {

        private Integer id;
        private String title;
        private Integer index;
        @JsonProperty("sheetType")
        private String type;


        public Properties() {
        }


        public Integer getId() {
            return id;
        }


        public void setId(Integer id) {
            this.id = id;
        }


        public String getTitle() {
            return title;
        }


        public void setTitle(String title) {
            this.title = title;
        }


        public Integer getIndex() {
            return index;
        }


        public void setIndex(Integer index) {
            this.index = index;
        }


        public String getType() {
            return type;
        }


        public void setType(String type) {
            this.type = type;
        }
    }
}
