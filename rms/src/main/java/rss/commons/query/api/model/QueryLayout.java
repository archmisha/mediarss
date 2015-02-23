package rss.commons.query.api.model;

import java.util.List;

/**
 * @author shai.nagar@hp.com
 *         Date: 3/18/13
 */
public interface QueryLayout extends QueryElement {

    /**
     * Returns the list of layout expressions.
     *
     * @return a list of Expression
     */
    List<Expression> getExpressions();

    /**
     * Adds one or more Expression to this query layout.
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
    <T> void acceptVisitor(QueryLayoutVisitor<T> visitor, T context);
}
