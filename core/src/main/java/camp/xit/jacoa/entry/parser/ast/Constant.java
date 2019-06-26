package camp.xit.jacoa.entry.parser.ast;

import camp.xit.jacoa.entry.parser.ParserConstants;
import camp.xit.jacoa.entry.parser.Token;

public class Constant extends Expression {

    private final String value;
    private final int kind;


    public Constant(Class<?> clazz, Token token) {
        super(clazz);
        this.value = token.image;
        this.kind = token.kind;
    }


    public String getValue() {
        switch (kind) {
            case ParserConstants.STRING_LITERAL:
                return value.substring(1, value.length() - 1);
            default:
                return value;
        }
    }


    public int getKind() {
        return kind;
    }


    @Override
    public String toString() {
        return value;
    }


    @Override
    public boolean filter(Object obj) {
        return true;
    }
}
