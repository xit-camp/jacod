package camp.xit.jacoa.entry.parser.ast;

import camp.xit.jacoa.entry.parser.ParserConstants;

public abstract class Expression {

    private final Class<?> entryClass;
    private final Expression left;
    private final Expression right;
    private final int operator;


    public Expression(Class<?> entryClass) {
        this(entryClass, null, null, -1);
    }


    public Expression(Class<?> entryClass, Expression expr) {
        this(entryClass, expr, null, -1);
    }


    public Expression(Class<?> entryClass, Expression left, Expression right, int operator) {
        this.entryClass = entryClass;
        this.left = left;
        this.right = right;
        this.operator = operator;
    }


    public Class<?> getEntryClass() {
        return entryClass;
    }


    protected Expression getLeft() {
        return left;
    }


    protected Expression getRight() {
        return right;
    }


    public int getOperator() {
        return operator;
    }

    public abstract boolean filter(Object obj);

    @Override
    public String toString() {
        return left.toString() + " " + (operator > -1 ? ParserConstants.tokenImage[operator].replace("\"", "") + " " + right.toString() : "");
    }
}
