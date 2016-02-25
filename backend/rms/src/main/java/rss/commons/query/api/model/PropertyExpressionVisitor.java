package rss.commons.query.api.model;

/**
 * This interface defines a visitor for model elements that contain {@link com.hp.maas.platform.commons.query.api.model.PropertyExpression}s.
 *
 * @param <T> Convenience context object type.
 * @author shai.nagar@hp.com
 *         Date: 4/25/13
 */
public interface PropertyExpressionVisitor<T> {

    /**
     * Visit callback for {@link PropertyExpression}
     *
     * @param expression a PropertyExpression
     * @param context    a variable context object
     */
    void visit(PropertyExpression expression, T context);

    /**
     * Same as {@link #visit(PropertyExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(PropertyExpression expression, T context);

}
