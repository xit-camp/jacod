package camp.xit.jacod.entry;

import camp.xit.jacod.entry.parser.ParseException;
import camp.xit.jacod.entry.parser.Parser;
import camp.xit.jacod.entry.parser.ast.Expression;
import camp.xit.jacod.model.Codelist;
import camp.xit.jacod.model.CodelistEntry;
import java.io.StringReader;

public class QueryEntryGroup<T extends CodelistEntry> implements EntryGroup<T> {

    private final String query;
    private Expression expression;
    private String objectId;


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

    public QueryEntryGroup(String objectId, Class<T> entryClass, String query) {
        this(entryClass, query);
        this.objectId = objectId;
    }


    @Override
    public Codelist<T> getEntries(Codelist<T> entries, boolean validOnly) {
        return entries.parallelStream(validOnly).filter(e -> expression.filter(e)).collect(Codelist.collect(entries.getName()));
    }

    public String getObjectId() {
        return objectId;
    }


    public String getQuery() {
        return query;
    }


    @Override
    public String toString() {
        return "QueryEntryGroup{" + "query=" + query  + ";objectId=" + objectId + "}";
    }
}
