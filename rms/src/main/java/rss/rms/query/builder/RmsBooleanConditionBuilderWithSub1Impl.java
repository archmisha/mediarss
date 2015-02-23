package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 20:09
 * To change this template use File | Settings | File Templates.
 */
class RmsBooleanConditionBuilderWithSub1Impl<T> extends RmsBaseBooleanConditionBuilderSupport<T> implements RmsBooleanConditionBuilderWithNestedLevel1<T> {

    RmsBooleanConditionBuilderWithSub1Impl(FilterBuilderContextManager ctx) {
        super(ctx);
    }


    @Override
    public RmsFilterBuilderWithNestedLevel1<T> and() {
        return (RmsFilterBuilderWithNestedLevel1<T>) super.and();
    }

    @Override
    public RmsFilterBuilderWithNestedLevel1<T> or() {
        return (RmsFilterBuilderWithNestedLevel1<T>) super.or();
    }

    @Override
    public RmsBooleanConditionBuilder<T> closeParen() {
        getContext().getExpressionBuilderHelper().doneSubTree();
        return getContext().getBooleanConditionBuilder();
    }

    @Override
    protected RmsBaseFilterBuilder<T> resolveFilterBuilder() {
        return getContext().getFilterBuilderWithSub1();
    }
}
