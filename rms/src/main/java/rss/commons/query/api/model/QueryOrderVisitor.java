package rss.commons.query.api.model;

/**
 * This interface defines a visitor for query model elements of type {@link QueryOrderImpl}. Since {@link QueryOrderImpl} objects
 * naturally hold {@link com.hp.maas.platform.commons.query.api.model.Expression} objects, this interface extend {@link com.hp.maas.platform.commons.query.api.model.ExpressionVisitor}.
 *
 * @param <T> Convenience context object type.
 * @author shai.nagar@hp.com
 *         Date: 3/2/13
 * @see com.hp.maas.platform.commons.query.api.model.ExpressionVisitor
 */
public interface QueryOrderVisitor<T> extends ExpressionVisitor<T> {

    /**
     * Visit callback for {@link QueryOrder}
     *
     * @param root    a QueryOrderImpl
     * @param context a variable context object
     */
    void visit(QueryOrder root, T context);

    /**
     * Visit callback for {@link OrderExpression}
     *
     * @param orderExpression an OrderExpression
     * @param context         a variable context object
     */
    void visit(OrderExpression orderExpression, T context);

}
