package rss.commons.query.api.model;

/**
 * Created by IntelliJ IDEA.
 * User: lokshin
 * Date: 8/15/13
 * Time: 2:28 PM
 */
public class NullCondition implements Condition {
    private Expression expression;
    private boolean not = false;

    /**
     * @return the {@link Expression}.
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
     * @return <code>true</code> if this is a NOT NULL condition.
     */
    public final boolean isNot() {
        return not;
    }

    /**
     * @param not specifies whether this is a NOT NULL condition.
     */
    public final void setNot(boolean not) {
        this.not = not;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder message = new StringBuilder();
        message.append(expression);
        message.append(SyntaxConst.SPACE);
        if (not) {
            message.append(SyntaxConst.OP_NOT_EQUAL);
        } else {
            message.append(SyntaxConst.OP_EQUAL);
        }
        message.append(SyntaxConst.SPACE);
        message.append(SyntaxConst.KW_NULL);
        return message.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        expression.acceptVisitor(visitor, context);
        visitor.exit(this, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return toApiString();
    }
}
