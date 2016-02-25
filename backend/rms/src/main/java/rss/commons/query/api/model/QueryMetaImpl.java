package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shai.nagar@hp.com
 *         Date: 4/25/13
 */
public class QueryMetaImpl implements QueryMeta {

    private final List<PropertyExpression> propertyExpressions = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    public final void addPropertyExpressions(PropertyExpression... propertyExpressions) {
        Collections.addAll(this.propertyExpressions, propertyExpressions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<PropertyExpression> getMetaProperties() {
        return Collections.unmodifiableList(propertyExpressions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> void acceptVisitor(QueryMetaVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        for (PropertyExpression propertyExpression : propertyExpressions) {
            visitor.visit(propertyExpression, context);
        }
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
    public final String toApiString() {
        return ApiStringUtils.appendApiCommaSeparatedList(propertyExpressions, new StringBuilder()).toString();
    }
}
