package rss.commons.query.api.model;

import java.util.List;

/**
 * @author shai.nagar@hp.com
 *         Date: 4/25/13
 */
public interface QueryMeta extends QueryElement {

    /**
     * Adds the specified {@link PropertyExpression}s to this object.
     *
     * @param propertyExpressions one or more {@link PropertyExpression}s to add.
     */
    void addPropertyExpressions(PropertyExpression... propertyExpressions);

    /**
     * Returns a list of queried "meta" properties.
     *
     * @return a list of PropertyExpression
     */
    List<PropertyExpression> getMetaProperties();

    /**
     * Recursively accepts a visitor on this query meta.
     *
     * @param visitor the visitor to accept.
     * @param context a generic context object.
     * @param <T>     the type of context object.
     */
    <T> void acceptVisitor(QueryMetaVisitor<T> visitor, T context);
}
