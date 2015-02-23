package rss.commons.query.api.model;

/**
 * Empty implementation of the {@link ExpressionVisitor} interface for convenience and easier backward compatibility
 * management of implementing classes.
 *
 * @author shai.nagar@hp.com
 *         Date: 9/3/13
 */
public abstract class AbstractExpressionVisitor<T> implements ExpressionVisitor<T> {

    @Override
    public void visit(BinaryExpression expression, T context) {
    }

    @Override
    public void exit(BinaryExpression expression, T context) {
    }

    @Override
    public void visit(UnaryExpression expression, T context) {
    }

    @Override
    public void exit(UnaryExpression expression, T context) {
    }

    @Override
    public void visit(NumberExpression expression, T context) {
    }

    @Override
    public void exit(NumberExpression expression, T context) {
    }

    @Override
    public void visit(StringExpression expression, T context) {
    }

    @Override
    public void exit(StringExpression expression, T context) {
    }

    @Override
    public void visit(BooleanExpression expression, T context) {
    }

    @Override
    public void exit(BooleanExpression expression, T context) {
    }


    @Override
    public void visit(VariableExpression expression, T context) {
    }

    @Override
    public void exit(VariableExpression expression, T context) {
    }

    @Override
    public void visit(FunctionExpression expression, T context) {
    }

    @Override
    public void exit(FunctionExpression expression, T context) {
    }

    @Override
    public void visit(WrappedExpression expression, T context) {
    }

    @Override
    public void exit(WrappedExpression expression, T context) {
    }

    @Override
    public void visit(PropertyExpression expression, T context) {
    }

    @Override
    public void exit(PropertyExpression expression, T context) {
    }

}
