package camp.xit.jacoa.entry.parser.ast;

public class AllExpression extends Expression {

    public AllExpression(Class<?> entryClass) {
        super(entryClass);
    }


    @Override
    public boolean filter(Object obj) {
        return true;
    }


    @Override
    public String toString() {
        return "ALL";
    }
}
