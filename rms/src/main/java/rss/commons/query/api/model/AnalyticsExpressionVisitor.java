package rss.commons.query.api.model;

/**
 * Created with IntelliJ IDEA.
 * User: sbruce
 * Date: 03/09/2013
 * Time: 10:16
 */
public interface AnalyticsExpressionVisitor<T> extends ExpressionVisitor<T> {

    /**
     * Visit callback for {@link AnalyticsFunctionExpression}
     *
     * @param expression a AnalyticsFunctionExpression
     * @param context    a variable context object
     */
    void visit(AnalyticsFunctionExpression expression, T context);

}
