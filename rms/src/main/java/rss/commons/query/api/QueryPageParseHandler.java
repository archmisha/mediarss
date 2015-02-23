package rss.commons.query.api;

import rss.commons.query.api.model.NumberExpression;
import rss.commons.query.api.model.QueryPage;
import rss.commons.query.generated.QueryParser;

/**
 * This class implements a {@link com.hp.maas.platform.commons.query.generated.QueryParserListener} (generated ANTLR
 * 4 interface) and used for parsing of {@link com.hp.maas.platform.commons.query.api.model.QueryPage} constructs.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/4/13
 */
class QueryPageParseHandler extends AbstractQueryParseHandler<QueryPage> {

    private QueryPage queryPage = ELEMENT_FACTORY.emptyPage();

    protected QueryPageParseHandler(QueryParser parser) {
        super(parser);
    }

    @Override
    public final void exitQueryPage(QueryParser.QueryPageContext ctx) {
        NumberExpression indexExpression = new NumberExpression(ctx.NUMBER(0).getText());
        NumberExpression sizeExpression = new NumberExpression(ctx.NUMBER(1).getText());

        queryPage.setOffset(parseParameterValue(indexExpression, "illegal.page.param", "offset"));
        queryPage.setSize(parseParameterValue(sizeExpression, "illegal.page.param", "size"));
    }

    @Override
    protected final QueryPage handleParsing() {
        parser.queryPage();
        return queryPage;
    }

    private int parseParameterValue(NumberExpression numberExpression, String messageKey, String... params) {
        int intValue;

        try {
            intValue = Integer.parseInt(numberExpression.getValue());
        } catch (NumberFormatException e) {
            throw new QueryParserException("Illegal page parameter expression." + params, e);
        }

        if (intValue < 0) {
            throw new QueryParserException("Page parameters cannot be negative." + params);
        }

        return intValue;
    }

}
