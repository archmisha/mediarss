package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class InCondition implements Condition {

    private Expression expression;
    private boolean not;
    private final List<Expression> expressionList = new ArrayList<>();

    /**
     * @return the IN condition {@link Expression}.
     */
    public final Expression getExpression() {
        return expression;
    }

    /**
     * @param expression the {@link Expression} to set.
     */
    public final void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * @return <code>true</code> if this is an "NOT IN" condition.
     */
    public final boolean isNot() {
        return not;
    }

    /**
     * @param not specifies whether this is an "NOT IN" condition.
     */
    public final void setNot(boolean not) {
        this.not = not;
    }

    /**
     * @return a {@link java.util.List} of {@link Expression}s.
     */
    public final List<Expression> getExpressionList() {
        return expressionList;
    }

    /**
     * @param expressionList a {@link java.util.List} of {@link Expression} to set for this condition.
     */
    public final void addExpressions(List<Expression> expressionList) {
        this.expressionList.addAll(expressionList);

    }

    /**
     * @param expressionList a {@link java.util.List} of {@link Expression} to set for this condition.
     */
    public final void addExpressions(Expression... expressionList) {
        Collections.addAll(this.expressionList, expressionList);
    }

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

        stringBuilder.append(SyntaxConst.KW_IN);
        stringBuilder.append(SyntaxConst.SPACE);
        stringBuilder.append(SyntaxConst.LPAREN);
        ApiStringUtils.appendApiCommaSeparatedList(expressionList, stringBuilder);
        stringBuilder.append(SyntaxConst.RPAREN);
        return stringBuilder.toString();
    }

    @Override
    public final String toString() {
        return toApiString();
    }

    @Override
    public final <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        this.expression.acceptVisitor(visitor, context);
        for (Expression inExpression : expressionList) {
            inExpression.acceptVisitor(visitor, context);
        }
        visitor.exit(this, context);
    }
}
