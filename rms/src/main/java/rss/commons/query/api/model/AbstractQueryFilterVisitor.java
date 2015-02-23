package rss.commons.query.api.model;

/**
 * Empty implementation of the {@link QueryFilterVisitor} interface for convenience and easier backward compatibility
 * management of implementing classes.
 * <p/>
 * User: victor.rosenberg@hp.com
 * Date: 5/23/13
 * Time: 9:45 AM
 */
public abstract class AbstractQueryFilterVisitor<T> extends AbstractConditionVisitor<T> implements QueryFilterVisitor<T> {

    @Override
    public void visit(QueryFilter root, T context) {
    }

}
