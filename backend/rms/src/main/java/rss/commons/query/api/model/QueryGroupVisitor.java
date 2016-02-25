package rss.commons.query.api.model;

/**
 * This interface defines a visitor for query model elements of type {@link com.hp.maas.platform.commons.query.api.model.QueryLayoutImpl}. {@link com.hp.maas.platform.commons.query.api.model.QueryLayoutImpl}
 * objects are basically {@link Expression} lists, therefore this interface extend {@link ExpressionVisitor}.
 *
 * @param <T> Convenience context object type.
 * @author shai.nagar@hp.com
 *         Date: 3/2/13
 * @see ExpressionVisitor
 */
public interface QueryGroupVisitor<T> extends ExpressionVisitor<T> {

    /**
     * Visit callback for {@link com.hp.maas.platform.commons.query.api.model.QueryGroupImpl}
     *
     * @param root    a QueryLayoutImpl
     * @param context a variable context object
     */
    void visit(QueryGroup root, T context);

}
