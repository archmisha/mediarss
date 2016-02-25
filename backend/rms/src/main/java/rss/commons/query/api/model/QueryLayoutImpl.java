package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/28/13
 */
class QueryLayoutImpl implements QueryLayout {

    protected final List<Expression> expressions = new ArrayList<>();

    /**
     * @return a {@link java.util.List} of assigned expressions.
     */
    @Override
    public final List<Expression> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    /**
     * @param expressions one or more {@link com.hp.maas.platform.commons.query.api.model.Expression}s to add.
     */
    @Override
    public final void addExpressions(Expression... expressions) {
        Collections.addAll(this.expressions, expressions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        return ApiStringUtils.appendApiCommaSeparatedList(expressions, new StringBuilder()).toString();
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
    public final <T> void acceptVisitor(QueryLayoutVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        for (Expression expression : expressions) {
            expression.acceptVisitor(visitor, context);
        }
    }
}
