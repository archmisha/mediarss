package rss.commons.query.api.model;

/**
 * @author shai.nagar@hp.com
 *         Date: 3/21/13
 */
public interface OrderExpression extends QueryElement {

    /**
     * Order directions enumeration.
     */
    public enum Direction implements QueryElement {
        /***/
        ASC(SyntaxConst.KW_ASC),
        /***/
        DESC(SyntaxConst.KW_DESC);

        private final String apiString;

        /**
         * Creates a new instance of {@link OrderExpression.Direction}.
         *
         * @param apiString the QueryImpl API representation string of this constant.
         */
        private Direction(String apiString) {
            this.apiString = apiString;
        }

        /**
         * @return the QueryImpl API string representation of this constant.
         */
        @Override
        public String toApiString() {
            return apiString;
        }
    }

    /**
     * @return the {@link com.hp.maas.platform.commons.query.api.model.Expression}
     */
    Expression getExpression();

    /**
     * @param expression an {@link com.hp.maas.platform.commons.query.api.model.Expression} to set.
     */
    void setExpression(Expression expression);

    /**
     * @return the order {@link OrderExpression.Direction}.
     */
    Direction getDirection();

    /**
     * @param direction a {@link OrderExpression.Direction} to set.
     */
    void setDirection(Direction direction);

    /**
     * Accepts a {@link QueryOrderVisitor} on this order expression and any nested expression.
     *
     * @param visitor the query order visitor to call visit on.
     * @param context a context object to pass to the visitor.
     * @param <T>     generic context object type.
     */
    <T> void acceptVisitor(QueryOrderVisitor<T> visitor, T context);
}
