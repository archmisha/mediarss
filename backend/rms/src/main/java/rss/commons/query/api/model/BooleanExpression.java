package rss.commons.query.api.model;

/**
 * @author shai.nagar@hp.com
 *         Date: 12/1/13
 */
public class BooleanExpression implements Expression {

    private boolean value;

    public BooleanExpression() {
    }

    public BooleanExpression(boolean value) {
        this.value = value;
    }

    public BooleanExpression(String value) {
        this.value = Boolean.valueOf(value);
    }

    public final boolean getValue() {
        return value;
    }

    public final void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public final <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        visitor.exit(this, context);
    }

    @Override
    public final String toApiString() {
        return String.valueOf(value);
    }
}
