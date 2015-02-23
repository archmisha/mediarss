package rss.commons.query.api.model;

/**
 * Created by IntelliJ IDEA.
 * User: lokshin
 * Date: 8/11/13
 * Time: 12:03 PM
 */
public class StartsWithCondition implements Condition {
    private boolean not = false;
    private Expression expression;
    private Expression pattern;

    /**
     * @return the {@link Expression}.
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * @param expression an {@link Expression} to set.
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * @return <code>true</code> if this is a NOT condition.
     */
    public boolean isNot() {
        return not;
    }

    /**
     * @param not specifies whether this is a NOT condition.
     */
    public void setNot(boolean not) {
        this.not = not;
    }

    /**
     * @return the pattern {@link Expression}.
     */
    public Expression getPattern() {
        return pattern;
    }

    /**
     * @param pattern a pattern {@link Expression} to set.
     */
    public void setPattern(Expression pattern) {
        this.pattern = pattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder message = new StringBuilder();
        if (expression != null) {
            message.append(expression);
        }
        message.append(SyntaxConst.SPACE);
        if (not) {
            message.append(SyntaxConst.OP_NOT);
        }
        message.append(SyntaxConst.KW_STARTSWITH);
        message.append(SyntaxConst.LPAREN);
        if (pattern != null) {
            message.append(pattern);
        }
        message.append(SyntaxConst.RPAREN);
        return message.toString();
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
    public final <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        expression.acceptVisitor(visitor, context);
        pattern.acceptVisitor(visitor, context);
        visitor.exit(this, context);
    }
}
