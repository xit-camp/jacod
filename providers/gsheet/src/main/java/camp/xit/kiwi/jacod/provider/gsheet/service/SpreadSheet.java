package camp.xit.kiwi.jacod.provider.gsheet.service;

import java.util.List;

public class SpreadSheet {

    private String spreadsheetId;
    private Properties properties;
    private List<Sheet> sheets;


    public SpreadSheet() {
    }


    public String getSpreadsheetId() {
        return spreadsheetId;
    }


    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }


    public Properties getProperties() {
        return properties;
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }


    public List<Sheet> getSheets() {
        return sheets;
    }


    public void setSheets(List<Sheet> sheets) {
        this.sheets = sheets;
    }

    public static class Properties {

        private String title;
        private String locale;
        private String autoRecalc;
        private String timeZone;


        public Properties() {
        }


        public String getTitle() {
            return title;
        }


        public void setTitle(String title) {
            this.title = title;
        }


        public String getLocale() {
            return locale;
        }


        public void setLocale(String locale) {
            this.locale = locale;
        }


        public String getAutoRecalc() {
            return autoRecalc;
        }


        public void setAutoRecalc(String autoRecalc) {
            this.autoRecalc = autoRecalc;
        }


        public String getTimeZone() {
            return timeZone;
        }


        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }
    }
}
