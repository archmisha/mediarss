package rss.commons.query.api.model;

/**
 * This class provides a container for all the top level supported query sections:
 * <ul>
 * <li>{@link QueryLayout}</li>
 * <li>{@link QueryFilter}</li>
 * <li>{@link QueryOrder}</li>
 * <li>{@link QueryPage}</li>
 * <li>{@link QueryMeta}</li>
 * </ul>
 *
 * @author shai.nagar@hp.com
 *         Date: 3/7/13
 */
public class QueryImpl implements Query {

    private QueryLayout layout;
    private QueryFilter filter;
    private QueryOrder order;
    private QueryPage page;
    private QueryMeta meta;
    private QueryGroup group;
    private int skip = -1;
    private int size = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getSkip() {
        return skip;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setSkip(int skip) {
        this.skip = skip;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getSize() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setSize(int size) {
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final QueryLayout getLayout() {
        return layout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setLayout(QueryLayout layout) {
        this.layout = layout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final QueryFilter getFilter() {
        return filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setFilter(QueryFilter filter) {
        this.filter = filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final QueryOrder getOrder() {
        return order;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setOrder(QueryOrder order) {
        this.order = order;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final QueryPage getPage() {
        return page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setPage(QueryPage page) {
        this.page = page;
    }

    /**
     * {@inheritDoc}
     */
    public QueryMeta getMeta() {
        return meta;
    }

    /**
     * {@inheritDoc}
     */
    public void setMeta(QueryMeta meta) {
        this.meta = meta;
    }

    public QueryGroup getGroup() {
        return group;
    }

    public void setGroup(QueryGroup group) {
        this.group = group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return
                "layout=" + layout +
                        ", filter=" + filter +
                        ", order=" + order +
                        ", page=" + page +
                        ", meta=" + meta +
                        ", skip=" + skip +
                        ", size=" + size +
                        ", group=" + group;

    }
}
