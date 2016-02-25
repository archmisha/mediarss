package rss.commons.query.api.model;

/**
 * This interface defines a visitor for query model elements of type {@link QueryPage}.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/13/13
 * @deprecated This class is deprecated. Use {@link Query#setSkip(int)} and {@link Query#setSize(int)} to implement paging.
 */
@Deprecated
public interface QueryPageVisitor<T> {

    /**
     * Visit callback for {@link QueryPage}
     *
     * @param root    a QueryPage
     * @param context a variable context object
     */
    void visit(QueryPage root, T context);
}
