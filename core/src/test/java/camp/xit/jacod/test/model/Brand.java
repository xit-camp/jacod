package camp.xit.jacod.test.model;

import camp.xit.jacod.model.CodelistEntry;
import camp.xit.jacod.model.CodelistEnum;

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
