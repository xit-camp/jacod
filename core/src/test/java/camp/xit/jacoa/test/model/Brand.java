package camp.xit.jacoa.test.model;

import camp.xit.jacoa.model.CodelistEntry;
import camp.xit.jacoa.model.CodelistEnum;

public class Brand extends CodelistEntry {

    public Brand() {
    }


    public Brand(String code) {
        super(code);
    }


    public Brand(CodelistEnum<? extends CodelistEntry> codeEnum) {
        super(codeEnum);
    }
}
