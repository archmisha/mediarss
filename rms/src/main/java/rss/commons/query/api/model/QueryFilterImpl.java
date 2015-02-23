package rss.commons.query.api.model;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
class QueryFilterImpl implements QueryFilter {

    private Condition condition = EmptyCondition.getINSTANCE();

    /**
     * @param condition the root {@link com.hp.maas.platform.commons.query.api.model.Condition} to set for this filter.
     */
    @Override
    public final void setCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * @return the root {@link com.hp.maas.platform.commons.query.api.model.Condition} which represents this filter.
     */
    @Override
    public final Condition getCondition() {
        return condition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        String str = "null";
        if (condition != null) {
            str = condition.toApiString();
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
    public final <T> void acceptVisitor(QueryFilterVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        condition.acceptVisitor(visitor, context);
    }
}
