package rss.commons.query.api.model;

/**
 * This interface defines a visitor for query model elements of type {@link QueryFilterImpl}. A {@link QueryFilterImpl} is a
 * wrapper of a {@link com.hp.maas.platform.commons.query.api.model.Condition} and therefore this interface is an extension of {@link com.hp.maas.platform.commons.query.api.model.ConditionVisitor}.
 *
 * @param <T> Convenience context object type.
 * @author shai.nagar@hp.com
 *         Date: 3/2/13
 * @see com.hp.maas.platform.commons.query.api.model.ConditionVisitor
 * @see com.hp.maas.platform.commons.query.api.model.ExpressionVisitor
 */
public interface QueryFilterVisitor<T> extends ConditionVisitor<T> {

    /**
     * Visit callback for {@link QueryFilterImpl}
     *
     * @param root    a QueryFilterImpl
     * @param context a variable context object
     */
    void visit(QueryFilter root, T context);

}
