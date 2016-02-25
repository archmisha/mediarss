package rss.commons.query.api.model;

/**
 * An empty condition to represent an empty or unset condition object.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/7/13
 */
class EmptyCondition implements Condition {

    private static final Condition INSTANCE = new EmptyCondition();

    static Condition getINSTANCE() {
        return INSTANCE;
    }

    private EmptyCondition() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
    }

}
