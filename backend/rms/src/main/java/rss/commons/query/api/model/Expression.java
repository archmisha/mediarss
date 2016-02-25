package rss.commons.query.api.model;

/**
 * This interface is the root of the query object model expression hierarchy.
 *
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public interface Expression extends QueryElement {

    /**
     * Accepts a {@link ExpressionVisitor} on this expression and any nested expression.
     *
     * @param visitor the expression visitor to call visit on.
     * @param context a context object to pass to the visitor.
     * @param <T>     generic context object type.
     */
    <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context);
}
