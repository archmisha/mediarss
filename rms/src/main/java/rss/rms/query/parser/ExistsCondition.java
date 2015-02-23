package rss.rms.query.parser;

import rss.commons.query.api.model.Condition;
import rss.commons.query.api.model.ConditionVisitor;

/**
 * @author Mark Bramnik
 *         Date: 02/06/13
 *         Time: 10:09
 */
public class ExistsCondition implements Condition {
    private String fieldPath;
    private boolean hasNegation;

    public ExistsCondition(String fieldPath, boolean hasNegation) {
        this.fieldPath = fieldPath;
        this.hasNegation = hasNegation;
    }

    public final String getFieldPath() {
        return fieldPath;
    }

    public final boolean hasNegation() {
        return hasNegation;
    }

    @Override
    public <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return toApiString();
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder();
        if (hasNegation()) {
            sb.append("not ");
        }
        sb.append("exists ").append(getFieldPath());
        return sb.toString();
    }
}
