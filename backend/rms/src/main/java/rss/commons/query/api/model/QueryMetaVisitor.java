package rss.commons.query.api.model;

/**
 * This interface defines a visitor for query model elements of type {@link QueryMeta}. Since {@link QueryMeta} objects
 * naturally hold {@link com.hp.maas.platform.commons.query.api.model.PropertyExpression} objects, this interface extend
 * {@link com.hp.maas.platform.commons.query.api.model.PropertyExpressionVisitor}.
 *
 * @param <T> Convenience context object type.
 * @author shai.nagar@hp.com
 *         Date: 4/25/13
 * @see com.hp.maas.platform.commons.query.api.model.PropertyExpressionVisitor
 */
public interface QueryMetaVisitor<T> extends PropertyExpressionVisitor<T> {

    /**
     * Visit callback for {@link QueryMeta}
     *
     * @param root    a QueryMeta
     * @param context a variable context object
     */
    void visit(QueryMeta root, T context);
}
