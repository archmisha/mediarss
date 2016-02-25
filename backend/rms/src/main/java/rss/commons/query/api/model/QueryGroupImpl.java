package rss.commons.query.api.model;

/**
 * Created by IntelliJ IDEA.
 * User: lokshin
 * Date: 7/10/13
 * Time: 10:44 AM
 */
public class QueryGroupImpl extends QueryLayoutImpl implements QueryGroup {
    @Override
    public final <T> void acceptVisitor(QueryGroupVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        for (Expression expression : expressions) {
            expression.acceptVisitor(visitor, context);
        }
    }
}
