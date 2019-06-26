package camp.xit.jacoa.entry;

import camp.xit.jacoa.entry.parser.ParseException;
import camp.xit.jacoa.entry.parser.Parser;
import camp.xit.jacoa.entry.parser.ast.Expression;
import camp.xit.jacoa.model.Codelist;
import camp.xit.jacoa.model.CodelistEntry;
import java.io.StringReader;

public class QueryEntryGroup<T extends CodelistEntry> implements EntryGroup<T> {

    private final String query;
    private Expression expression;


    public QueryEntryGroup(Class<T> entryClass, String query) {
        this.query = query;
        if (query != null) {
            try {
                Parser parser = new Parser(new StringReader(query));
                expression = parser.expression(entryClass);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid query syntax: " + e.getMessage(), e);
            }
        }
    }


    @Override
    public Codelist<T> getEntries(Codelist<T> entries, boolean validOnly) {
        return entries.parallelStream(validOnly).filter(e -> expression.filter(e)).collect(Codelist.collect(entries.getName()));
    }


    public String getQuery() {
        return query;
    }


    @Override
    public String toString() {
        return "QueryEntryGroup{" + "query=" + query + '}';
    }
}
