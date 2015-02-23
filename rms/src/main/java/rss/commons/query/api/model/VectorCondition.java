package rss.commons.query.api.model;

import static rss.commons.query.api.model.SyntaxConst.*;

/**
 * @author shai.nagar@hp.com
 *         Date: 6/9/13
 */
public class VectorCondition implements Condition {

    private String vectorName;
    private Condition condition;
    private boolean notexists = false;

    /**
     * Returns the name of the vector the condition should be evaluated against.
     *
     * @return a vector name.
     */
    public final String getVectorName() {
        return vectorName;
    }

    /**
     * Sets the name of the vector the condition should be evaluated against.
     *
     * @param vectorName a vector name.
     */
    public final void setVectorName(String vectorName) {
        this.vectorName = vectorName;
    }

    /**
     * Returns the condition to evaluate against the vector objects.
     *
     * @return a Condition
     */
    public final Condition getCondition() {
        return condition;
    }

    /**
     * Sets the condition to evaluate against the vector objects.
     *
     * @param condition a {@link Condition} to set.
     */
    public final void setCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        condition.acceptVisitor(visitor, context);
        visitor.exit(this, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (notexists) {
            stringBuilder.append(VECTOR_NOTEXISTS);
        }
        if (vectorName != null) {
            stringBuilder.append(vectorName);
        }
        stringBuilder.append(SQUARELPAREN);
        if (condition != null) {
            stringBuilder.append(condition);
        }
        stringBuilder.append(SQUARERPAREN);
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return toApiString();
    }

    public boolean isNotexists() {
        return notexists;
    }

    public void setNotexists(boolean notexists) {
        this.notexists = notexists;
    }
}
