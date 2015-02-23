package rss.rms.query.builder;


import rss.rms.query.FilterInformationImpl;
import rss.rms.query.expression.RmsQueryExpression;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 10/05/13
 * Time: 17:26
 * To change this template use File | Settings | File Templates.
 */
class RmsBooleanConditionBuilderImpl<T> extends RmsBaseBooleanConditionBuilderSupport<T> implements RmsBooleanConditionBuilder<T> {

    public RmsBooleanConditionBuilderImpl(FilterBuilderContextManager<T> ctx) {
        super(ctx);
    }

    @Override
    public RmsFilterBuilder<T> and() {
        return (RmsFilterBuilder<T>) super.and();
    }

    @Override
    public RmsFilterBuilder<T> or() {
        return (RmsFilterBuilder<T>) super.or();
    }

    @Override
    public RmsQueryBuilder<T> done() {
        RmsQueryExpression expressionProduct = getContext().getExpressionBuilderHelper().getExpression();
        getContext().getQueryBuilder().setFilterInformation(new FilterInformationImpl(expressionProduct));
        return getContext().getQueryBuilder();
    }

    @Override
    protected RmsBaseFilterBuilder<T> resolveFilterBuilder() {
        return getContext().getFilterBuilder();
    }
}
