package rss.commons.query.api.model;

import static rss.commons.query.api.model.SyntaxConst.LPAREN;
import static rss.commons.query.api.model.SyntaxConst.RPAREN;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class WrappedCondition implements Condition {

    private Condition condition;

    /**
     * Creates a new instance of {@link WrappedCondition}.
     */
    public WrappedCondition() {
        //PMD: Intentionally empty constructor, please set required fields later by setter methods.
    }

    /**
     * @return the {@link com.hp.maas.platform.commons.query.api.model.Condition}
     */
    public final Condition getCondition() {
        return condition;
    }

    /**
     * @param condition a {@link com.hp.maas.platform.commons.query.api.model.Condition} to set.
     */
    public final void setCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(LPAREN);
        if (condition != null) {
            stringBuilder.append(condition.toApiString());
        }
        stringBuilder.append(RPAREN);
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
        condition.acceptVisitor(visitor, context);
        visitor.exit(this, context);
    }
}
