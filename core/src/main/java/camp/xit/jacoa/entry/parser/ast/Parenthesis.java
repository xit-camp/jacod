package camp.xit.jacoa.entry.parser.ast;

public class Parenthesis extends Expression {

    public Parenthesis(Class<?> clazz, Expression expr) {
        super(clazz, expr);
    }


    @Override
    public boolean filter(Object obj) {
        return getLeft().filter(obj);
    }


    @Override
    public String toString() {
        return "(" + getLeft() + ")";
    }
}
