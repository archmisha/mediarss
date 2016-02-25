package rss.rms.query.builder;

/**
 * @author Mark Bramnik
 *         Date: 4/23/13
 *         Time: 10:14 AM
 * @since 1.0.0-9999
 */
class RmsFilterBuilderImpl<T> extends RmsBaseFilterBuilderSupport<T> implements RmsFilterBuilder<T> {

    RmsFilterBuilderImpl(FilterBuilderContextManager<T> ctx) {
        super(ctx);
    }


    protected RmsBooleanConditionBuilder<T> resolveBooleanConditionBuilder() {
        return getContext().getBooleanConditionBuilder();
    }

    @Override
    public RmsBooleanConditionBuilder<T> less(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilder<T>) super.less(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> less(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilder<T>) super.less(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> less(String fieldPath, String value) {
        return (RmsBooleanConditionBuilder<T>) super.less(fieldPath, value);
    }


    @Override
    public RmsBooleanConditionBuilder<T> greater(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilder<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> greater(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilder<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> greater(String fieldPath, String value) {
        return (RmsBooleanConditionBuilder<T>) super.greater(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> lessOrEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilder<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> lessOrEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilder<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> lessOrEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilder<T>) super.lessOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilder<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilder<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilder<T>) super.greaterOrEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> equal(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilder<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> equal(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilder<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> equal(String fieldPath, Boolean value) {
        return (RmsBooleanConditionBuilder<T>) super.equal(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> equal(String fieldPath, String value) {
        return (RmsBooleanConditionBuilder<T>) super.equal(fieldPath, value);
    }


    @Override
    public RmsBooleanConditionBuilder<T> notEqual(String fieldPath, Integer value) {
        return (RmsBooleanConditionBuilder<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notEqual(String fieldPath, Long value) {
        return (RmsBooleanConditionBuilder<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notEqual(String fieldPath, Boolean value) {
        return (RmsBooleanConditionBuilder<T>) super.notEqual(fieldPath, value);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notEqual(String fieldPath, String value) {
        return (RmsBooleanConditionBuilder<T>) super.notEqual(fieldPath, value);
    }


    @Override
    public RmsBooleanConditionBuilder<T> in(String fieldPath, Integer... values) {
        return (RmsBooleanConditionBuilder<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilder<T> in(String fieldPath, Long... values) {
        return (RmsBooleanConditionBuilder<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilder<T> in(String fieldPath, String... values) {
        return (RmsBooleanConditionBuilder<T>) super.in(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notIn(String fieldPath, Integer... values) {
        return (RmsBooleanConditionBuilder<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notIn(String fieldPath, Long... values) {
        return (RmsBooleanConditionBuilder<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notIn(String fieldPath, String... values) {
        return (RmsBooleanConditionBuilder<T>) super.notIn(fieldPath, values);
    }

    @Override
    public RmsBooleanConditionBuilder<T> between(String fieldPath, Integer leftBound, Integer rightBound) {
        return (RmsBooleanConditionBuilder<T>) super.between(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilder<T> between(String fieldPath, Long leftBound, Long rightBound) {
        return (RmsBooleanConditionBuilder<T>) super.between(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilder<T> exist(String fieldPath) {
        return (RmsBooleanConditionBuilder<T>) super.exist(fieldPath);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notBetween(String fieldPath, Integer leftBound, Integer rightBound) {
        return (RmsBooleanConditionBuilder<T>) super.notBetween(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notBetween(String fieldPath, Long leftBound, Long rightBound) {
        return (RmsBooleanConditionBuilder<T>) super.notBetween(fieldPath, leftBound, rightBound);
    }

    @Override
    public RmsBooleanConditionBuilder<T> notExist(String fieldPath) {
        return (RmsBooleanConditionBuilder<T>) super.notExist(fieldPath);
    }

    @Override
    public RmsBooleanConditionBuilder<T> startsWith(String fieldPath, String value) {
        return (RmsBooleanConditionBuilder<T>) super.startsWith(fieldPath, value);
    }

    @Override
    public RmsFilterBuilderWithNestedLevel1<T> openParen() {
        getContext().getExpressionBuilderHelper().startSubTree();
        return getContext().getFilterBuilderWithSub1();
    }

    /*@Override
    public RmsQueryBuilder<T> done() {
        return getContext().getQueryBuilder();
    }*/
}
