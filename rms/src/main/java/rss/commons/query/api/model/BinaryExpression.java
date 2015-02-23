package rss.commons.query.api.model;

import static rss.commons.query.api.model.SyntaxConst.*;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class BinaryExpression implements Expression {

    /**
     * Supported binary operators enumeration.
     */
    public enum Operator implements QueryElement {
        /***/
        MULTIPLY(OP_MULTIPLY),
        /***/
        MODULO(OP_MODULO),
        /***/
        DIVIDE(OP_DIVIDE),
        /***/
        ADD(OP_ADD),
        /***/
        SUB(OP_SUB);

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
     * @return the binary {@link com.hp.maas.platform.commons.query.api.model.BinaryExpression.Operator}.
     */
    public final Operator getOperator() {
        return operator;
    }

    /**
     * @param operator the {@link com.hp.maas.platform.commons.query.api.model.BinaryExpression.Operator} to set.
     */
    public final void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * @return the left hand {@link com.hp.maas.platform.commons.query.api.model.Expression}.
     */
    public final Expression getLeftExpression() {
        return leftExpression;
    }

    /**
     * @param leftExpression the left hand {@link com.hp.maas.platform.commons.query.api.model.Expression} to set.
     */
    public final void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    /**
     * @return the right hand {@link com.hp.maas.platform.commons.query.api.model.Expression}.
     */
    public final Expression getRightExpression() {
        return rightExpression;
    }

    /**
     * @param rightExpression the right hand {@link com.hp.maas.platform.commons.query.api.model.Expression} to set.
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
        if (operator != null) {
            stringBuilder.append(SPACE);
            stringBuilder.append(operator.toApiString());
            stringBuilder.append(SPACE);
        }
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
    public final <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        leftExpression.acceptVisitor(visitor, context);
        rightExpression.acceptVisitor(visitor, context);
        visitor.exit(this, context);
    }

}
