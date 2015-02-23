package rss.commons.query.api.model;

/**
 * Empty implementation of the {@link ConditionVisitor} interface for convenience and easier backward compatibility
 * management of implementing classes.
 *
 * @author shai.nagar@hp.com
 *         Date: 9/3/13
 * @see AbstractExpressionVisitor
 */
public abstract class AbstractConditionVisitor<T> extends AbstractExpressionVisitor<T> implements ConditionVisitor<T> {

    @Override
    public void visit(LogicalCondition condition, T context) {
    }

    @Override
    public void exit(LogicalCondition condition, T context) {
    }

    @Override
    public void visit(ComparisonCondition condition, T context) {
    }

    @Override
    public void exit(ComparisonCondition condition, T context) {
    }

    @Override
    public void visit(StartsWithCondition condition, T context) {
    }

    @Override
    public void exit(StartsWithCondition condition, T context) {
    }

    @Override
    public void visit(NullCondition condition, T context) {
    }

    @Override
    public void exit(NullCondition condition, T context) {
    }

    @Override
    public void visit(BetweenCondition condition, T context) {
    }

    @Override
    public void exit(BetweenCondition condition, T context) {
    }

    @Override
    public void visit(InCondition condition, T context) {
    }

    @Override
    public void exit(InCondition condition, T context) {
    }

    @Override
    public void visit(VectorCondition condition, T context) {
    }

    @Override
    public void exit(VectorCondition condition, T context) {
    }

    @Override
    public void visit(WrappedCondition condition, T context) {
    }

    @Override
    public void exit(WrappedCondition condition, T context) {
    }

    @Override
    public void visit(Condition condition, T context) {
    }

    @Override
    public void exit(Condition condition, T context) {
    }

}
