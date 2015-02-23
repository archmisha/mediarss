package rss.commons.query.api.model;

/**
 * @author shai.nagar@hp.com
 *         Date: 3/18/13
 * @deprecated {@link QueryPage} is deprecated. Use {@link Query#setSkip(int)} and {@link Query#setSize(int)} instead.
 */
@Deprecated
public interface QueryPage extends QueryElement {

    /**
     * Returns the page size.
     *
     * @return an integer. In case the page is unset -1 is returned.
     */
    int getSize();

    /**
     * Sets the page size.
     *
     * @param pageSize the page size to set.
     */
    void setSize(int pageSize);

    /**
     * Returns the global page offset.
     *
     * @return an integer. In case the page is unset -1 is returned.
     */
    int getOffset();

    /**
     * Sets the global page offset.
     *
     * @param pageOffset the page offset to set.
     */
    void setOffset(int pageOffset);

    /**
     * Recursively accepts a visitor on this query page.
     *
     * @param visitor the visitor to accept.
     * @param context a generic context object.
     * @param <T>     the type of context object.
     */
    <T> void acceptVisitor(QueryPageVisitor<T> visitor, T context);
}
