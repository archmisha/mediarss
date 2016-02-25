package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 13/05/13
 * Time: 06:52
 * To change this template use File | Settings | File Templates.
 */
class RmsFilterBuilderWithNestedLevel1Impl<T> extends RmsBaseFilterBuilderSupport<T> implements RmsFilterBuilderWithNestedLevel1<T> {


    RmsFilterBuilderWithNestedLevel1Impl(FilterBuilderContextManager<T> ctx) {
        super(ctx);
    }

    protected RmsBooleanConditionBuilderWithNestedLevel1<T> resolveBooleanConditionBuilder() {
        return getContext().getBooleanConditionBuilderWithSub1();
    }


    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> less(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.less(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> less(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.less(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> less(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.less(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> greater(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> greater(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> greater(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> lessOrEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> lessOrEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> lessOrEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> greaterOrEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> greaterOrEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> greaterOrEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> equal(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> equal(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> equal(String fieldPath, Boolean value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> equal(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.equal(fieldPath, value);
    }


    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notEqual(String fieldPath, Boolean value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notEqual(fieldPath, value);
    }


    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> in(String fieldPath, Integer... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> in(String fieldPath, Long... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> in(String fieldPath, String... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notIn(String fieldPath, Integer... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notIn(String fieldPath, Long... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notIn(String fieldPath, String... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> between(String fieldPath, Integer leftBound, Integer rightBound) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.between(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> between(String fieldPath, Long leftBound, Long rightBound) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.between(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> exist(String fieldPath) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.exist(fieldPath);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notBetween(String fieldPath, Integer leftBound, Integer rightBound) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notBetween(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notBetween(String fieldPath, Long leftBound, Long rightBound) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notBetween(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> notExist(String fieldPath) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.notExist(fieldPath);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> startsWith(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel1<T>) super.startsWith(fieldPath, value);
    }

    @Override
    public RmsFilterBuilderWithNestedLevel2<T> openParen() {
        getContext().getExpressionBuilderHelper().startSubTree();
        return getContext().getFilterBuilderWithSub2();
    }


}
