package rss.rms.query.builder;

/**
 * The builder responsible for condition and the second level of nested access
 *
 * @author Mark Bramnik
 */
class RmsFilterBuilderWithNestedLevel2Impl<T> extends RmsBaseFilterBuilderSupport<T> implements RmsFilterBuilderWithNestedLevel2<T> {

    RmsFilterBuilderWithNestedLevel2Impl(FilterBuilderContextManager<T> ctx) {
        super(ctx);
    }

    @Override
    protected RmsBooleanConditionBuilderWithNestedLevel2<T> resolveBooleanConditionBuilder() {
        return getContext().getBooleanConditionBuilderWithSub2();
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> less(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.less(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> less(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.less(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> less(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.less(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> greater(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> greater(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> greater(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> lessOrEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> lessOrEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> lessOrEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> greaterOrEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> greaterOrEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> greaterOrEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> equal(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> equal(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> equal(String fieldPath, Boolean value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> equal(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.equal(fieldPath, value);
    }


    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notEqual(String fieldPath, Boolean value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notEqual(fieldPath, value);
    }


    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> in(String fieldPath, Integer... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> in(String fieldPath, Long... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> in(String fieldPath, String... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notIn(String fieldPath, Integer... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notIn(String fieldPath, Long... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notIn(String fieldPath, String... values) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> between(String fieldPath, Integer leftBound, Integer rightBound) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.between(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> between(String fieldPath, Long leftBound, Long rightBound) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.between(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> exist(String fieldPath) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.exist(fieldPath);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notBetween(String fieldPath, Integer leftBound, Integer rightBound) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notBetween(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notBetween(String fieldPath, Long leftBound, Long rightBound) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notBetween(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> notExist(String fieldPath) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.notExist(fieldPath);
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> startsWith(String fieldPath, String value) {
        return (RmsBooleanConditionBuilderWithNestedLevel2<T>) super.startsWith(fieldPath, value);
    }
}
