package camp.xit.jacod.entry.parser.ast;

public abstract class LogicalExpression extends Expression {

    public LogicalExpression(Class<?> clazz, Expression arg1, Expression arg2, int operator) {
        super(clazz, arg1, arg2, operator);
    }
}
