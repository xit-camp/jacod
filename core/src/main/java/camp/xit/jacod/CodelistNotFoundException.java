package camp.xit.jacod;

import camp.xit.jacod.model.CodelistEntry;

public class CodelistNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codelist;
    private final String message;


    public CodelistNotFoundException(String codelist, String message) {
        this.message = message;
        this.codelist = codelist;
    }


    public CodelistNotFoundException(Class<? extends CodelistEntry> entryClass, String message) {

        this.message = message;
        this.codelist = entryClass.getName();
    }


    public String getCodelist() {
        return codelist;
    }


    @Override
    public String getMessage() {
        return message;
    }
}
