package camp.xit.jacod.entry.parser.ast;

import camp.xit.jacod.entry.parser.ParserConstants;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class UniCondition extends Condition {

    public UniCondition(Class<?> clazz, Property property, int operator) {
        super(clazz, property, null, operator);
    }


    @Override
    public boolean filter(Object entry) {
        boolean result = false;
        Object objValue = getValue(entry);
        switch (getOperator()) {
        case ParserConstants.IS_EMPTY:
            return objValue == null;
        }
        return result;
    }
}
