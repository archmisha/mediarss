/**
 *
 */
package rss.commons.query.api.model;

import static rss.commons.query.api.model.SyntaxConst.COLON;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class VariableExpression implements Expression {

    private String name;

    /**
     * Creates a new instance of VariableExpression
     */
    public VariableExpression() {
    }

    /**
     * Creates a new instance of {@link VariableExpression}.
     *
     * @param name the name of the variable.
     */
    public VariableExpression(String name) {
        this.name = name;
    }

    /**
     * @param name the variable name to set.
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * @return the variable name.
     */
    public final String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        return COLON + name;
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
