package rss.commons.query.api.model;

/**
 * This interface defines a visitor for query model elements of type {@link com.hp.maas.platform.commons.query.api.model.Condition}. Since {@link com.hp.maas.platform.commons.query.api.model.Condition} objects
 * naturally hold {@link Expression} objects, this interface extends {@link ExpressionVisitor}.
 *
 * @param <T> Convenience context object type.
 * @author shai.nagar@hp.com
 *         Date: 3/2/13
 * @see ExpressionVisitor
 */
public interface ConditionVisitor<T> extends ExpressionVisitor<T> {

    /**
     * Visit callback for {@link LogicalCondition}
     *
     * @param condition a LogicalCondition
     * @param context   a variable context object
     */
    void visit(LogicalCondition condition, T context);

    /**
     * Same as {@link #visit(LogicalCondition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(LogicalCondition condition, T context);

    /**
     * Visit callback for {@link com.hp.maas.platform.commons.query.api.model.ComparisonCondition}
     *
     * @param condition a ComparisonCondition
     * @param context   a variable context object
     */
    void visit(ComparisonCondition condition, T context);

    /**
     * Same as {@link #visit(ComparisonCondition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(ComparisonCondition condition, T context);

    /**
     * Visit callback for {@link StartsWithCondition}
     *
     * @param condition a StartsWithCondition
     * @param context   a variable context object
     */
    void visit(StartsWithCondition condition, T context);

    /**
     * Same as {@link #visit(StartsWithCondition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(StartsWithCondition condition, T context);

    /**
     * Visit callback for {@link com.hp.maas.platform.commons.query.api.model.NullCondition}
     *
     * @param condition a NullCondition
     * @param context   a variable context object
     */
    void visit(NullCondition condition, T context);

    /**
     * Same as {@link #visit(NullCondition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(NullCondition condition, T context);

    /**
     * Visit callback for {@link com.hp.maas.platform.commons.query.api.model.BetweenCondition}
     *
     * @param condition a BetweenCondition
     * @param context   a variable context object
     */
    void visit(BetweenCondition condition, T context);

    /**
     * Same as {@link #visit(BetweenCondition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(BetweenCondition condition, T context);

    /**
     * Visit callback for {@link InCondition}
     *
     * @param condition a InCondition
     * @param context   a variable context object
     */
    void visit(InCondition condition, T context);

    /**
     * Same as {@link #visit(InCondition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(InCondition condition, T context);

    /**
     * Visit callback for {@link VectorCondition}
     *
     * @param condition a VectorCondition
     * @param context   a variable context object
     */
    void visit(VectorCondition condition, T context);

    /**
     * Same as {@link #visit(VectorCondition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(VectorCondition condition, T context);

    /**
     * Visit callback for {@link WrappedCondition}
     *
     * @param condition a WrappedCondition
     * @param context   a variable context object
     */
    void visit(WrappedCondition condition, T context);

    /**
     * Same as {@link #visit(WrappedCondition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(WrappedCondition condition, T context);

    /**
     * Visit callback for {@link Condition}
     *
     * @param condition a Condition
     * @param context   a variable context type
     */
    void visit(Condition condition, T context);

    /**
     * Same as {@link #visit(Condition, Object)}. This method is called after all sub elements are visited and
     * after the corresponding 'visit' method has returned.
     */
    void exit(Condition condition, T context);

}
