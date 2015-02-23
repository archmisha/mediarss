package rss.rms.query.builder;

import rss.rms.query.expression.ExpressionFactory;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 21/05/13
 * Time: 08:13
 * To change this template use File | Settings | File Templates.
 */
abstract class RmsBaseBooleanConditionBuilderSupport<T> extends ExpressionBuilderContextManagerSupport<T> implements RmsBaseBooleanConditionBuilder<T> {

    public RmsBaseBooleanConditionBuilderSupport(FilterBuilderContextManager<T> ctx) {
        super(ctx);
    }

    protected abstract RmsBaseFilterBuilder<T> resolveFilterBuilder();

    @Override
    public RmsBaseFilterBuilder<T> and() {
        getContext().getExpressionBuilderHelper().addLogicalExpression(ExpressionFactory.createAndExpression());
        return resolveFilterBuilder();
    }

    @Override
    public RmsBaseFilterBuilder<T> or() {
        getContext().getExpressionBuilderHelper().addLogicalExpression(ExpressionFactory.createOrExpression());
        return resolveFilterBuilder();
    }

}
