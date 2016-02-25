package rss.commons.query.api;


import rss.commons.query.api.model.Expression;
import rss.commons.query.api.model.QueryElement;
import rss.commons.query.api.model.QueryGroup;
import rss.commons.query.generated.QueryParser;

/**
 * Created by IntelliJ IDEA.
 * User: lokshin
 * Date: 7/10/13
 * Time: 10:41 AM
 */
public class QueryGroupParseHandler extends AbstractQueryParseHandler<QueryGroup> {

    private final QueryGroup queryGroupBy = ELEMENT_FACTORY.emptyGroupBy();

    protected QueryGroupParseHandler(QueryParser parser) {
        super(parser);
    }

    @Override
    public void exitQueryGroupBy(QueryParser.QueryGroupByContext ctx) {
        for (QueryElement queryElement : elementStack) {
            queryGroupBy.addExpressions((Expression) queryElement);
        }
    }

    @Override
    protected QueryGroup handleParsing() {
        parser.queryGroupBy();
        return queryGroupBy;
    }
}
