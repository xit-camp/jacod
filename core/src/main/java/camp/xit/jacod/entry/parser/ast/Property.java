package camp.xit.jacod.entry.parser.ast;

public class Property extends Expression {

    private final String property;


    public Property(Class<?> clazz, String value) {
        super(clazz);
        this.property = value;
    }


    public String getProperty() {
        return property;
    }


    @Override
    public String toString() {
        return property;
    }


    @Override
    public boolean filter(Object obj) {
        return true;
    }
}
