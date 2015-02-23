package rss.rms.query.parser;

import rss.commons.query.api.model.Expression;
import rss.commons.query.api.model.ExpressionVisitor;

/**
 * @author Mark Bramnik
 *         Date: 02/06/13
 *         Time: 10:33
 */
public class BooleanExpression implements Expression {
    private boolean value;

    public BooleanExpression(boolean value) {
        this.value = value;
    }

    public final String getValue() {
        return Boolean.toString(value);
    }

    @Override
    public <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context) {
        // do nothing
    }

    @Override
    public String toApiString() {
        return Boolean.toString(value);
    }
}
