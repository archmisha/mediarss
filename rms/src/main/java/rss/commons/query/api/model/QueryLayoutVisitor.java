package rss.commons.query.api.model;

/**
 * This interface defines a visitor for query model elements of type {@link QueryLayoutImpl}. {@link QueryLayoutImpl}
 * objects are basically {@link com.hp.maas.platform.commons.query.api.model.Expression} lists, therefore this interface extend {@link com.hp.maas.platform.commons.query.api.model.ExpressionVisitor}.
 *
 * @param <T> Convenience context object type.
 * @author shai.nagar@hp.com
 *         Date: 3/2/13
 * @see com.hp.maas.platform.commons.query.api.model.ExpressionVisitor
 */
public interface QueryLayoutVisitor<T> extends ExpressionVisitor<T> {

    /**
     * Visit callback for {@link QueryLayoutImpl}
     *
     * @param root    a QueryLayoutImpl
     * @param context a variable context object
     */
    void visit(QueryLayout root, T context);

}
