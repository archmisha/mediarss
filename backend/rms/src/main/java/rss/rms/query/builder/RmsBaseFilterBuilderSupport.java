package rss.rms.query.builder;


import rss.rms.query.expression.ExpressionBuilderHelper;

import static rss.rms.query.expression.ExpressionFactory.*;

/**
 * Contains base implementation for all of the filter builders
 *
 * @author Mark Bramnik
 */
public abstract class RmsBaseFilterBuilderSupport<T> extends ExpressionBuilderContextManagerSupport<T> implements RmsBaseFilterBuilder<T> {


    protected RmsBaseFilterBuilderSupport(FilterBuilderContextManager<T> ctx) {
        super(ctx);
    }

    protected abstract RmsBaseBooleanConditionBuilder<T> resolveBooleanConditionBuilder();


    private ExpressionBuilderHelper getBuilderHelper() {
        return getContext().getExpressionBuilderHelper();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> less(String fieldPath, Integer value) {
        getBuilderHelper().addTerminalExpression(createLTExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> less(String fieldPath, Long value) {
        getBuilderHelper().addTerminalExpression(createLTExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> less(String fieldPath, String value) {
        getBuilderHelper().addTerminalExpression(createLTExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> greater(String fieldPath, Integer value) {
        getBuilderHelper().addTerminalExpression(createGTExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> greater(String fieldPath, Long value) {
        getBuilderHelper().addTerminalExpression(createGTExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> greater(String fieldPath, String value) {
        getBuilderHelper().addTerminalExpression(createGTExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> lessOrEqual(String fieldPath, Integer value) {
        getBuilderHelper().addTerminalExpression(createLTEExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> lessOrEqual(String fieldPath, Long value) {
        getBuilderHelper().addTerminalExpression(createLTEExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> lessOrEqual(String fieldPath, String value) {
        getBuilderHelper().addTerminalExpression(createLTEExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, Integer value) {
        getBuilderHelper().addTerminalExpression(createGTEExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, Long value) {
        getBuilderHelper().addTerminalExpression(createGTEExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, String value) {
        getBuilderHelper().addTerminalExpression(createGTEExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }


    @Override
    public RmsBaseBooleanConditionBuilder<T> equal(String fieldPath, Integer value) {
        getBuilderHelper().addTerminalExpression(createEQExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> equal(String fieldPath, Long value) {
        getBuilderHelper().addTerminalExpression(createEQExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> equal(String fieldPath, Boolean value) {
        getBuilderHelper().addTerminalExpression(createEQExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> equal(String fieldPath, String value) {
        getBuilderHelper().addTerminalExpression(createEQExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notEqual(String fieldPath, Integer value) {
        getBuilderHelper().addTerminalExpression(createNotEqExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notEqual(String fieldPath, Long value) {
        getBuilderHelper().addTerminalExpression(createNotEqExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notEqual(String fieldPath, Boolean value) {
        getBuilderHelper().addTerminalExpression(createNotEqExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notEqual(String fieldPath, String value) {
        getBuilderHelper().addTerminalExpression(createNotEqExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> in(String fieldPath, Integer... values) {
        getBuilderHelper().addTerminalExpression(createInExpression(fieldPath, values));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> in(String fieldPath, Long... values) {
        getBuilderHelper().addTerminalExpression(createInExpression(fieldPath, values));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> in(String fieldPath, String... values) {
        getBuilderHelper().addTerminalExpression(createInExpression(fieldPath, values));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notIn(String fieldPath, Integer... values) {
        getBuilderHelper().addTerminalExpression(createNotInExpression(fieldPath, values));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notIn(String fieldPath, Long... values) {
        getBuilderHelper().addTerminalExpression(createNotInExpression(fieldPath, values));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notIn(String fieldPath, String... values) {
        getBuilderHelper().addTerminalExpression(createNotInExpression(fieldPath, values));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> between(String fieldPath, Integer leftBound, Integer rightBound) {
        getBuilderHelper().addTerminalExpression(createBetweenExpression(fieldPath, leftBound, rightBound));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> between(String fieldPath, Long leftBound, Long rightBound) {
        getBuilderHelper().addTerminalExpression(createBetweenExpression(fieldPath, leftBound, rightBound));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notBetween(String fieldPath, Integer leftBound, Integer rightBound) {
        getBuilderHelper().addTerminalExpression(createNotBetweenExpression(fieldPath, leftBound, rightBound));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notBetween(String fieldPath, Long leftBound, Long rightBound) {
        getBuilderHelper().addTerminalExpression(createNotBetweenExpression(fieldPath, leftBound, rightBound));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> exist(String fieldPath) {
        getBuilderHelper().addTerminalExpression(createExistsExpression(fieldPath));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> notExist(String fieldPath) {
        getBuilderHelper().addTerminalExpression(createNotExistsExpression(fieldPath));
        return resolveBooleanConditionBuilder();
    }

    @Override
    public RmsBaseBooleanConditionBuilder<T> startsWith(String fieldPath, String value) {
        getBuilderHelper().addTerminalExpression(createStartsWithExpression(fieldPath, value));
        return resolveBooleanConditionBuilder();
    }
}
