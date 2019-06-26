package camp.xit.jacoa.entry.parser.ast;

import camp.xit.jacoa.entry.parser.ParserConstants;
import camp.xit.jacoa.model.CodelistEntry;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Condition extends Expression {

    private List<Method> getters;
    private Pattern exprPattern;


    public Condition(Class<?> clazz, Property property, Constant rightExpression, int operator) {
        super(clazz, property, rightExpression, operator);
        this.getters = compile();
    }


    public Property getProperty() {
        return (Property) getLeft();
    }


    public Constant getRightExpression() {
        return (Constant) getRight();
    }


    private List<Method> compile() {
        String[] properties = getProperty().getProperty().split("\\.");
        Class<?> clazz = getEntryClass();
        List<Method> getters = new ArrayList<>();
        for (String property : properties) {
            try {
                Method getter = new PropertyDescriptor(property, clazz).getReadMethod();
                getters.add(getter);
                clazz = getter.getReturnType();
            } catch (IntrospectionException e) {
                throw new CompileException("Can't compile condition. No getter for " + clazz.getSimpleName() + "." + property);
            }
        }
        if (getOperator() == ParserConstants.MATCH) {
            exprPattern = Pattern.compile(unescape(getRightExpression().getValue()));
        }
        return getters;
    }


    public boolean filter(Object entry) {
        boolean result = false;
        Object objValue = getValue(entry);
        String exprValue = unescape(getRightExpression().getValue());
        Class<?> returnType = getters.get(getters.size() - 1).getReturnType();
        if (returnType.equals(String.class) || CodelistEntry.class.isAssignableFrom(returnType)
                || Collection.class.isAssignableFrom(returnType)) {
            boolean collection = false;
            Set<String> objValues = Collections.emptySet();
            if (CodelistEntry.class.isAssignableFrom(returnType)) {
                CodelistEntry refEntry = (CodelistEntry) objValue;
                objValue = refEntry != null ? refEntry.getCode() : null;
            } else if (Collection.class.isAssignableFrom(returnType)) {
                collection = true;
                objValues = new HashSet<>();
                for (Object obj : ((Collection) objValue)) {
                    if (obj instanceof CodelistEntry) {
                        objValues.add(((CodelistEntry) obj).getCode());
                    }
                }
            }
            switch (getOperator()) {
                case ParserConstants.EQUALS:
                    result = collection ? objValues.contains(exprValue) : exprValue.equals(objValue);
                    break;
                case ParserConstants.NOT_EQUALS:
                    result = !(collection ? objValues.contains(exprValue) : exprValue.equals(objValue));
                    break;
                case ParserConstants.MATCH:
                    if (collection) {
                        throw new RuntimeException("Regexp match operator is not supported in collections!");
                    }
                    if (!(objValue instanceof String)) {
                        throw new RuntimeException("Regexp match is allowed only for String properties");
                    }
                    result = exprPattern.matcher((String) objValue).matches();
                    break;
                default:
                    throw new RuntimeException("Invalid operator for string value "
                            + getEntryClass().getSimpleName() + "." + getProperty());
            }
        } else if (Number.class.isAssignableFrom(returnType)) {
            BigDecimal numValue = new BigDecimal(exprValue);
            BigDecimal objNumValue = objValue != null ? new BigDecimal(String.valueOf(objValue)) : null;
            if (objValue != null) {
                switch (getOperator()) {
                    case ParserConstants.EQUALS:
                        result = numValue.compareTo(objNumValue) == 0;
                        break;
                    case ParserConstants.NOT_EQUALS:
                        result = !numValue.equals(objNumValue);
                        break;
                    case ParserConstants.GT:
                        result = numValue.compareTo(objNumValue) < 0;
                        break;
                    case ParserConstants.GTE:
                        result = numValue.compareTo(objNumValue) <= 0;
                        break;
                    case ParserConstants.LT:
                        result = numValue.compareTo(objNumValue) > 0;
                        break;
                    case ParserConstants.LTE:
                        result = numValue.compareTo(objNumValue) >= 0;
                        break;
                    default:
                        throw new RuntimeException("Invalid operator for string value "
                                + getEntryClass().getSimpleName() + "." + getProperty());
                }
            }
        } else if (returnType.equals(Boolean.class) || returnType.equals(Boolean.TYPE)) {
            result = objValue != null ? objValue.equals(Boolean.parseBoolean(exprValue)) : false;
        } else {
            throw new RuntimeException("Invalid property type for "
                    + getEntryClass().getSimpleName() + "." + getProperty() + ". Supported are strings and numbers.");
        }
        return result;
    }


    public Object getValue(Object entry) {
        Object obj = entry;
        try {
            for (Method m : getters) {
                obj = m.invoke(obj);
                if (obj == null) break;
            }
            return obj;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Cannot evaluate query", e);
        }
    }


    public static String unescape(String input) {
        StringBuilder builder = new StringBuilder();

        int i = 0;
        while (i < input.length()) {
            char delimiter = input.charAt(i);
            i++; // consume letter or backslash

            if (delimiter == '\\' && i < input.length()) {

                // consume first after backslash
                char ch = input.charAt(i);
                i++;

                if (ch == '\\' || ch == '/' || ch == '"' || ch == '\'') {
                    builder.append(ch);
                } else if (ch == 'n') builder.append('\n');
                else if (ch == 'r') builder.append('\r');
                else if (ch == 't') builder.append('\t');
                else if (ch == 'b') builder.append('\b');
                else if (ch == 'f') builder.append('\f');
                else if (ch == 'u') {

                    StringBuilder hex = new StringBuilder();

                    // expect 4 digits
                    if (i + 4 > input.length()) {
                        throw new RuntimeException("Not enough unicode digits! ");
                    }
                    for (char x : input.substring(i, i + 4).toCharArray()) {
                        if (!Character.isLetterOrDigit(x)) {
                            throw new RuntimeException("Bad character in unicode escape.");
                        }
                        hex.append(Character.toLowerCase(x));
                    }
                    i += 4; // consume those four digits.

                    int code = Integer.parseInt(hex.toString(), 16);
                    builder.append((char) code);
                } else {
                    throw new RuntimeException("Illegal escape sequence: \\" + ch);
                }
            } else { // it's not a backslash, or it's the last character.
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }
}
