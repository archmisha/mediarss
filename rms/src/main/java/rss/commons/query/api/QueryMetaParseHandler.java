package rss.commons.query.api;

import rss.commons.query.api.model.PropertyExpression;
import rss.commons.query.api.model.QueryElement;
import rss.commons.query.api.model.QueryMeta;
import rss.commons.query.generated.QueryParser;

/**
 * This class implements a {@link com.hp.maas.platform.commons.query.generated.QueryParserListener} (generated ANTLR
 * 4 interface) and used for parsing of {@link com.hp.maas.platform.commons.query.api.model.QueryMeta} constructs.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/4/13
 */
class QueryMetaParseHandler extends AbstractQueryParseHandler<QueryMeta> {

    private final QueryMeta queryMeta = ELEMENT_FACTORY.emptyMeta();

    protected QueryMetaParseHandler(QueryParser parser) {
        super(parser);
    }

    @Override
    public void exitQueryMeta(QueryParser.QueryMetaContext ctx) {
        for (QueryElement queryElement : elementStack) {
            queryMeta.addPropertyExpressions((PropertyExpression) queryElement);
        }
    }

    @Override
    protected QueryMeta handleParsing() {
        parser.queryMeta();
        return queryMeta;
    }
}
