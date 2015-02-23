package rss.commons.query.api.model;

/**
 * This interface is the root of the query object model condition hierarchy.
 *
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public interface Condition extends QueryElement {

    /**
     * Accepts a {@link ConditionVisitor} on this condition and any nested expression.
     *
     * @param visitor the condition visitor to call visit on.
     * @param context a context object to pass to the visitor.
     * @param <T>     generic context object type.
     */
    <T> void acceptVisitor(ConditionVisitor<T> visitor, T context);
}
