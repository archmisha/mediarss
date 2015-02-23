package rss.commons.query.api;

import rss.commons.query.api.model.*;
import rss.commons.query.generated.QueryParser;

/**
 * This class implements a {@link com.hp.maas.platform.commons.query.generated.QueryParserListener} (generated ANTLR
 * 4 interface) and used for parsing of {@link com.hp.maas.platform.commons.query.api.model.QueryOrder} constructs.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/4/13
 */
class QueryOrderParseHandler extends AbstractQueryParseHandler<QueryOrder> {

    private final QueryOrder queryOrder = ELEMENT_FACTORY.emptyOrder();

    protected QueryOrderParseHandler(QueryParser parser) {
        super(parser);
    }

    @Override
    public void exitOrderItem(QueryParser.OrderItemContext ctx) {
        OrderExpression orderExpression = new OrderExpressionImpl();
        if (ctx.ASC() != null) {
            orderExpression.setDirection(OrderExpression.Direction.ASC);
        } else if (ctx.DESC() != null) {
            orderExpression.setDirection(OrderExpression.Direction.DESC);
        }

        orderExpression.setExpression((Expression) elementStack.pop());
        elementStack.push(orderExpression);
    }

    @Override
    public void exitQueryOrder(QueryParser.QueryOrderContext ctx) {
        for (QueryElement queryElement : elementStack) {
            queryOrder.addExpressions((OrderExpression) queryElement);
        }
    }

    @Override
    protected QueryOrder handleParsing() {
        parser.queryOrder();
        return queryOrder;
    }
}
