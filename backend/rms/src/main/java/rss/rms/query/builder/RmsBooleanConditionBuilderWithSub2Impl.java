package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 20:10
 * To change this template use File | Settings | File Templates.
 */
class RmsBooleanConditionBuilderWithSub2Impl<T> extends RmsBaseBooleanConditionBuilderSupport<T> implements RmsBooleanConditionBuilderWithNestedLevel2<T> {

    RmsBooleanConditionBuilderWithSub2Impl(FilterBuilderContextManager<T> ctx) {
        super(ctx);
    }

    @Override
    public RmsFilterBuilderWithNestedLevel2<T> and() {
        return (RmsFilterBuilderWithNestedLevel2<T>) super.and();
    }

    @Override
    public RmsFilterBuilderWithNestedLevel2<T> or() {
        return (RmsFilterBuilderWithNestedLevel2<T>) super.or();
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> closeParen() {
        getContext().getExpressionBuilderHelper().doneSubTree();
        return getContext().getBooleanConditionBuilderWithSub1();
    }

    @Override
    protected RmsBaseFilterBuilder<T> resolveFilterBuilder() {
        return getContext().getFilterBuilderWithSub2();
    }
}
