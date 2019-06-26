package camp.xit.jacoa;

import camp.xit.jacoa.model.CodelistEntry;

public class EntryNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codelist;
    private final String code;


    public EntryNotFoundException(String codelist, String code) {
        super("Codelist entry '" + code + "' not found for codelist " + codelist);
        this.codelist = codelist;
        this.code = code;
    }


    public EntryNotFoundException(Class<? extends CodelistEntry> entryClass, String code) {
        super("Codelist entry '" + code + "' not found for codelist " + entryClass.getName());
        this.codelist = entryClass.getName();
        this.code = code;
    }


    public EntryNotFoundException(String codelist, String code, Throwable cause) {
        super("Codelist entry '" + code + "' not found for codelist " + codelist, cause);
        this.codelist = codelist;
        this.code = code;
    }


    public String getCodelist() {
        return codelist;
    }


    public String getCode() {
        return code;
    }
}
