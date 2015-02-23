package rss.commons.query.api.model;

import static rss.commons.query.api.model.SyntaxConst.LPAREN;
import static rss.commons.query.api.model.SyntaxConst.RPAREN;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class WrappedExpression implements Expression {

    private Expression expression;

    /**
     * Creates a new instance of {@link WrappedExpression}.
     */
    public WrappedExpression() {
        //PMD: Intentionally empty constructor, please set required fields later by setter methods.
    }

    /**
     * @return the {@link Expression}
     */
    public final Expression getExpression() {
        return expression;
    }

    /**
     * @param expression an {@link Expression} to set.
     */
    public final void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(LPAREN);
        if (expression != null) {
            stringBuilder.append(expression.toApiString());
        }

        stringBuilder.append(RPAREN);
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return toApiString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        expression.acceptVisitor(visitor, context);
        visitor.exit(this, context);
    }
}
