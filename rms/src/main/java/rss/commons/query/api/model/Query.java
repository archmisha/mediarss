package rss.commons.query.api.model;

/**
 * This is the topmost element of the query object model. It is basically a convenience interface to allow consumers to
 * encapsulate several query segments in to one object. Currently the parser does not support parsing a full query
 * statement into an object.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/18/13
 * @see QueryLayout
 * @see QueryFilter
 * @see QueryOrder
 * @see QueryMeta
 */
public interface Query {

    /**
     * Returns the number of results to be skipped.
     *
     * @return an integer
     */
    int getSkip();

    /**
     * Sets the number of results to be skipped
     *
     * @param skip an integer
     */
    void setSkip(int skip);

    /**
     * Returns the number of results to be returned
     *
     * @return an integer
     */
    int getSize();

    /**
     * Sets the number of results to be returned
     *
     * @param size an integer
     */
    void setSize(int size);

    /**
     * Returns the layout section of this query.
     *
     * @return a QueryLayout
     */
    QueryLayout getLayout();

    /**
     * Sets the layout section of this query
     *
     * @param layout the QueryLayout
     */
    void setLayout(QueryLayout layout);

    /**
     * Returns the group section of this query.
     *
     * @return a QueryGroup
     */
    QueryGroup getGroup();

    /**
     * Sets the group section of this query
     *
     * @param layout the QueryGroup
     */
    void setGroup(QueryGroup layout);

    /**
     * Returns the filter section of this query.
     *
     * @return a QueryFilter
     */
    QueryFilter getFilter();

    /**
     * Sets the filter section of this query
     *
     * @param filter the QueryLayout
     */
    void setFilter(QueryFilter filter);

    /**
     * Returns the order section of this query.
     *
     * @return a QueryOrder
     */
    QueryOrder getOrder();

    /**
     * Sets the order section of this query
     *
     * @param order the QueryLayout
     */
    void setOrder(QueryOrder order);

    /**
     * Returns the page section of this query.
     *
     * @return a QueryPage
     * @deprecated {@link QueryPage} is deprecated. Use {@link Query#setSkip(int)} and {@link Query#setSize(int)} instead.
     */
    @Deprecated
    QueryPage getPage();

    /**
     * Sets the page section of this query
     *
     * @param page the QueryPage
     * @deprecated {@link QueryPage} is deprecated. Use {@link Query#setSkip(int)} and {@link Query#setSize(int)} instead.
     */
    @Deprecated
    void setPage(QueryPage page);

    /**
     * Returns the meta section of this query.
     *
     * @return a QueryMeta
     */
    QueryMeta getMeta();

    /**
     * Sets the meta section of this query.
     *
     * @param meta the QueryMeta
     */
    void setMeta(QueryMeta meta);
}
