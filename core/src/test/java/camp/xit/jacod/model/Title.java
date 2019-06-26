package camp.xit.jacod.model;

import camp.xit.jacod.model.CodelistEnum;
import camp.xit.jacod.model.CodelistEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Title extends CodelistEntry {

    public enum Position {
        BEFORE, AFTER
    }

    private Position position;


    public Title() {
    }


    public Title(String code) {
        super(code);
    }


    public Title(CodelistEnum<Title> codeEnum) {
        super(codeEnum.toString());
    }
}
