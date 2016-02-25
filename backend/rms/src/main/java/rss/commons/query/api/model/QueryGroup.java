package rss.commons.query.api.model;

import java.util.List;

/**
 * User: benzeev
 * Date: 03/07/2013
 */
public interface QueryGroup extends QueryElement {

    /**
     * Returns the list of group expressions.
     *
     * @return a list of Expression
     */
    List<Expression> getExpressions();

    /**
     * Adds one or more Expression to this query group.
     *
     * @param expressions one or more Expressions to add.
     */
    void addExpressions(Expression... expressions);

    /**
     * Recursively accepts a visitor on this query layout.
     *
     * @param visitor the visitor to accept.
     * @param context a generic context object.
     * @param <T>     the type of context object.
     */
    <T> void acceptVisitor(QueryGroupVisitor<T> visitor, T context);
}
