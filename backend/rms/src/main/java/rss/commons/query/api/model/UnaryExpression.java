package rss.commons.query.api.model;

import static rss.commons.query.api.model.SyntaxConst.OP_MINUS;
import static rss.commons.query.api.model.SyntaxConst.OP_PLUS;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class UnaryExpression implements Expression {

    /**
     * Unary operators enumeration.
     */
    public enum Operator implements QueryElement {
        /***/
        PLUS(OP_PLUS),
        /***/
        MINUS(OP_MINUS);

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
    private Expression expression;

    /**
     * Creates a new instance of {@link UnaryExpression}.
     */
    public UnaryExpression() {
        //PMD: Intentionally empty constructor, please set required fields later by setter methods.
    }

    /**
     * @return the {@link com.hp.maas.platform.commons.query.api.model.UnaryExpression.Operator}
     */
    public final Operator getOperator() {
        return operator;
    }

    /**
     * @param operator an {@link com.hp.maas.platform.commons.query.api.model.UnaryExpression.Operator} to set.
     */
    public final void setOperator(Operator operator) {
        this.operator = operator;
    }

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
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder message = new StringBuilder();
        if (operator != null) {
            message.append(operator.toApiString());
        }
        if (expression != null) {
            message.append(expression.toApiString());
        }
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
    public final <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        expression.acceptVisitor(visitor, context);
    }
}
