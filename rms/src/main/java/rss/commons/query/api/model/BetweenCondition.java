/**
 *
 */
package rss.commons.query.api.model;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class BetweenCondition implements Condition {

    private boolean not = false;

    private Expression expression;
    private Expression leftBoundary;
    private Expression rightBoundary;

    /**
     * @return the leftBoundary
     */
    public final Expression getLeftBoundary() {
        return leftBoundary;
    }

    /**
     * @param leftBoundary the leftBoundary to set
     */
    public final void setLeftBoundary(Expression leftBoundary) {
        this.leftBoundary = leftBoundary;
    }

    /**
     * @return the rightBoundary
     */
    public final Expression getRightBoundary() {
        return rightBoundary;
    }

    /**
     * @param rightBoundary the rightBoundary to set
     */
    public final void setRightBoundary(Expression rightBoundary) {
        this.rightBoundary = rightBoundary;
    }

    /**
     * @return the expression
     */
    public final Expression getExpression() {
        return expression;
    }

    /**
     * @param expression the expression to set
     */
    public final void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * @return the not
     */
    public final boolean isNot() {
        return not;
    }

    /**
     * @param not the not to set
     */
    public final void setNot(boolean not) {
        this.not = not;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (expression != null) {
            stringBuilder.append(expression);
            stringBuilder.append(SyntaxConst.SPACE);
        }
        if (not) {
            stringBuilder.append(SyntaxConst.OP_NOT);
        }

        stringBuilder.append(SyntaxConst.KW_BETWEEN);
        stringBuilder.append(SyntaxConst.SPACE);
        stringBuilder.append(SyntaxConst.LPAREN);
        if (leftBoundary != null) {
            stringBuilder.append(leftBoundary);
        }
        stringBuilder.append(SyntaxConst.COMMA);
        if (rightBoundary != null) {
            stringBuilder.append(rightBoundary);
        }
        stringBuilder.append(SyntaxConst.RPAREN);
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
    public final <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        expression.acceptVisitor(visitor, context);
        leftBoundary.acceptVisitor(visitor, context);
        rightBoundary.acceptVisitor(visitor, context);
        visitor.exit(this, context);
    }
}
