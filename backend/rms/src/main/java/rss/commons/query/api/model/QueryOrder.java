package rss.commons.query.api.model;

import java.util.List;

/**
 * @author shai.nagar@hp.com
 *         Date: 3/18/13
 */
public interface QueryOrder extends QueryElement {

    /**
     * Returns the list of order expressions.
     *
     * @return a list of OrderExpressions
     */
    List<OrderExpression> getOrderExpressions();

    /**
     * Adds one or more OrderExpression to this query order.
     *
     * @param orderExpressions one or more OrderExpressions to add.
     */
    void addExpressions(OrderExpression... orderExpressions);

    /**
     * Recursively accepts a visitor on this query order.
     *
     * @param visitor the visitor to accept.
     * @param context a generic context object.
     * @param <T>     the type of context object.
     */
    <T> void acceptVisitor(QueryOrderVisitor<T> visitor, T context);
}
