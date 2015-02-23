package rss.commons.query.api.model;

import static rss.commons.query.api.model.SyntaxConst.SPACE;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class OrderExpressionImpl implements OrderExpression {

    private Expression expression;
    private Direction direction;

    /**
     * @return the {@link com.hp.maas.platform.commons.query.api.model.Expression}
     */
    @Override
    public final Expression getExpression() {
        return expression;
    }

    /**
     * @param expression an {@link com.hp.maas.platform.commons.query.api.model.Expression} to set.
     */
    @Override
    public final void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * @return the order {@link OrderExpressionImpl.Direction}.
     */
    @Override
    public final Direction getDirection() {
        return direction;
    }

    /**
     * @param direction a {@link OrderExpressionImpl.Direction} to set.
     */
    @Override
    public final void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (expression != null) {
            stringBuilder.append(expression.toApiString());
        }
        if (direction != null) {
            stringBuilder.append(SPACE);
            stringBuilder.append(direction.toApiString());
        }
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
    public final <T> void acceptVisitor(QueryOrderVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        expression.acceptVisitor(visitor, context);
    }
}
