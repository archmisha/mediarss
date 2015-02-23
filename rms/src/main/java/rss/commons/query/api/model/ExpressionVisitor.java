package rss.commons.query.api.model;

/**
 * This interface defines a visitor for query model elements of type {@link com.hp.maas.platform.commons.query.api.model.Expression}.
 *
 * @param <T> Convenience context object type.
 * @author shai.nagar@hp.com
 *         Date: 3/2/13
 */
public interface ExpressionVisitor<T> extends PropertyExpressionVisitor<T> {

    /**
     * Visit callback for {@link com.hp.maas.platform.commons.query.api.model.BinaryExpression}
     *
     * @param expression a BinaryExpression
     * @param context    a variable context object
     */
    void visit(BinaryExpression expression, T context);

    /**
     * Same as {@link #visit(BinaryExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(BinaryExpression expression, T context);

    /**
     * Visit callback for {@link UnaryExpression}
     *
     * @param expression a UnaryExpression
     * @param context    a variable context object
     */
    void visit(UnaryExpression expression, T context);

    /**
     * Same as {@link #visit(UnaryExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(UnaryExpression expression, T context);

    /**
     * Visit callback for {@link NumberExpression}
     *
     * @param expression a NumberExpression
     * @param context    a variable context object
     */
    void visit(NumberExpression expression, T context);

    /**
     * Same as {@link #visit(NumberExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(NumberExpression expression, T context);

    /**
     * Visit callback for {@link StringExpression}
     *
     * @param expression a StringExpression
     * @param context    a variable context object
     */
    void visit(StringExpression expression, T context);

    /**
     * Same as {@link #visit(StringExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(StringExpression expression, T context);

    /**
     * Visit callback for {@link BooleanExpression}
     *
     * @param expression a BooleanExpression
     * @param context    a variable context object
     */
    void visit(BooleanExpression expression, T context);

    /**
     * Same as {@link #visit(BooleanExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(BooleanExpression expression, T context);

    /**
     * Visit callback for {@link VariableExpression}
     *
     * @param expression a VariableExpression
     * @param context    a variable context object
     */
    void visit(VariableExpression expression, T context);

    /**
     * Same as {@link #visit(VariableExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(VariableExpression expression, T context);

    /**
     * Visit callback for {@link FunctionExpression}
     *
     * @param expression a FunctionExpression
     * @param context    a variable context object
     */
    void visit(FunctionExpression expression, T context);

    /**
     * Same as {@link #visit(FunctionExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(FunctionExpression expression, T context);

    /**
     * Visit callback for {@link WrappedExpression}
     *
     * @param expression a WrappedExpression
     * @param context    a variable context object
     */
    void visit(WrappedExpression expression, T context);

    /**
     * Same as {@link #visit(WrappedExpression, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(WrappedExpression expression, T context);

    /**
     * Visit callback for {@link AnalyticsFunctionExpression}
     *
     * @param expression a AnalyticsFunctionExpression
     * @param context    a variable context object
     */
    void visit(AnalyticsFunctionExpression expression, T context);

}
