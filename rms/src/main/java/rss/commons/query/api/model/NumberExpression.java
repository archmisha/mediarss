package rss.commons.query.api.model;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class NumberExpression implements Expression {

    private String value;

    /**
     * Constructs a new instance without setting a value. Use the {@code setName(String value)} to set a numeric value.
     */
    public NumberExpression() {
    }

    /**
     * Constructs a new instance with the specified value.
     *
     * @param value a numeric value to set.
     */
    public NumberExpression(String value) {
        this.value = value;
    }

    /**
     * @return a numeric value as string. Can be a long SQL number expression and a scientific
     * numeric expression. Example: 123e4
     */
    public final String getValue() {
        return value;
    }

    /**
     * @param value a numeric value to set. Can be a long SQL number expression and a scientific
     *              numeric expression. Example: 123e4
     */
    public final void setValue(String value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        String str = "null";
        if (value != null) {
            str = value;
        }
        return str;
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
    }
}
