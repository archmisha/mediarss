package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
class QueryOrderImpl implements QueryOrder {

    private final List<OrderExpression> orderExpressions = new ArrayList<>();

    /**
     * @return the list of {@link OrderExpression}s assigned.
     */
    @Override
    public final List<OrderExpression> getOrderExpressions() {
        return orderExpressions;
    }

    /**
     * @param orderExpressions one or more {@link OrderExpression}s to add.
     */
    @Override
    public final void addExpressions(OrderExpression... orderExpressions) {
        Collections.addAll(this.orderExpressions, orderExpressions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        ApiStringUtils.appendApiCommaSeparatedList(orderExpressions, stringBuilder);
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return toApiString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> void acceptVisitor(QueryOrderVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        for (OrderExpression expr : orderExpressions) {
            expr.acceptVisitor(visitor, context);
        }
    }
}
