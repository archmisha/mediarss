package rss.commons.query.api.model;

/**
 * @author shai.nagar@hp.com
 *         Date: 3/18/13
 */
public interface QueryFilter extends QueryElement {

    /**
     * Sets the filtering condition of this query filter.
     *
     * @param condition a Condition
     */
    void setCondition(Condition condition);

    /**
     * Returns the filtering condition of this query filter.
     *
     * @return a Condition
     */
    Condition getCondition();

    /**
     * Recursively accepts a visitor on this query filter.
     *
     * @param visitor the visitor to accept.
     * @param context a generic context object.
     * @param <T>     the type of context object.
     */
    <T> void acceptVisitor(QueryFilterVisitor<T> visitor, T context);
}
