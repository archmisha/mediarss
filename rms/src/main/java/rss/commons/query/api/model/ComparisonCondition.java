package rss.commons.query.api.model;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class ComparisonCondition implements Condition {

    /**
     * Supported comparison operators enumeration.
     */
    public enum Operator implements QueryElement {
        /***/
        EQUALS(SyntaxConst.OP_EQUAL),
        /***/
        NOTEQUAL(SyntaxConst.OP_NOT_EQUAL),
        /***/
        LESS(SyntaxConst.OP_LESS),
        /***/
        GREATER(SyntaxConst.OP_GREATER),
        /***/
        LESSOREQUALS(SyntaxConst.OP_LESS_OR_EQUAL),
        /***/
        GREATEROREQUALS(SyntaxConst.OP_GREATER_OR_EQUAL);

        private final String apiString;

        private Operator(String apiString) {
            this.apiString = apiString;
        }

        /**
         * @return the QueryImpl API string representation of this operator.
         */
        @Override
        public String toApiString() {
            return apiString;
        }
    }

    private Operator operator;
    private Expression leftExpression;
    private Expression rightExpression;

    /**
     * @return the comparison {@link com.hp.maas.platform.commons.query.api.model.ComparisonCondition.Operator}
     */
    public final Operator getOperator() {
        return operator;
    }

    /**
     * @param operator the comparison {@link com.hp.maas.platform.commons.query.api.model.ComparisonCondition.Operator} to set.
     */
    public final void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * @return the left hand {@link Expression}.
     */
    public final Expression getLeftExpression() {
        return leftExpression;
    }

    /**
     * @param leftExpression the left hand {@link Expression} to set.
     */
    public final void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    /**
     * @return the right hand {@link Expression}.
     */
    public final Expression getRightExpression() {
        return rightExpression;
    }

    /**
     * @param rightExpression the right hand {@link Expression} to set.
     */
    public final void setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (leftExpression != null) {
            stringBuilder.append(leftExpression.toApiString());
        }
        stringBuilder.append(SyntaxConst.SPACE);
        if (operator != null) {
            stringBuilder.append(operator.toApiString());
        }
        stringBuilder.append(SyntaxConst.SPACE);
        if (rightExpression != null) {
            stringBuilder.append(rightExpression.toApiString());
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
    public final <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        leftExpression.acceptVisitor(visitor, context);
        rightExpression.acceptVisitor(visitor, context);
        visitor.exit(this, context);
    }
}
