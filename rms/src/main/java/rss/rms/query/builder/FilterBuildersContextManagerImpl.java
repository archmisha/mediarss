package rss.rms.query.builder;

import rss.rms.query.expression.ExpressionBuilderHelper;

/**
 * Aggregates the information about all the builders in the current query
 *
 * @author Mark Bramnik
 */
public class FilterBuildersContextManagerImpl<T> implements FilterBuilderContextManager<T> {

    private ModifiableRmsQueryBuilder<T> queryBuilder;
    private ExpressionBuilderHelper expressionBuilderHelper;


    // nulls - lazy initialized
    private RmsFilterBuilder<T> filterBuilder;
    private RmsFilterBuilderWithNestedLevel1<T> filterBuilderWithSub1;
    private RmsFilterBuilderWithNestedLevel2<T> filterBuilderWithSub2;
    private RmsBooleanConditionBuilder<T> booleanConditionBuilder;
    private RmsBooleanConditionBuilderWithNestedLevel1<T> booleanConditionBuilderWithSub1;
    private RmsBooleanConditionBuilderWithNestedLevel2<T> booleanConditionBuilderWithSub2;


    public FilterBuildersContextManagerImpl(ModifiableRmsQueryBuilder<T> rmsQueryBuilder) {
        this.queryBuilder = rmsQueryBuilder;
        this.expressionBuilderHelper = new ExpressionBuilderHelper();
    }

    @Override
    public ModifiableRmsQueryBuilder<T> getQueryBuilder() {
        return queryBuilder;
    }

    @Override
    public RmsFilterBuilder<T> getFilterBuilder() {
        if (filterBuilder == null) {
            filterBuilder = new RmsFilterBuilderImpl<>(this);
        }
        return filterBuilder;
    }

    @Override
    public RmsFilterBuilderWithNestedLevel1<T> getFilterBuilderWithSub1() {
        if (filterBuilderWithSub1 == null) {
            filterBuilderWithSub1 = new RmsFilterBuilderWithNestedLevel1Impl<>(this);
        }
        return filterBuilderWithSub1;
    }

    @Override
    public RmsFilterBuilderWithNestedLevel2<T> getFilterBuilderWithSub2() {
        if (filterBuilderWithSub2 == null) {
            filterBuilderWithSub2 = new RmsFilterBuilderWithNestedLevel2Impl<>(this);
        }
        return filterBuilderWithSub2;
    }

    @Override
    public RmsBooleanConditionBuilder<T> getBooleanConditionBuilder() {
        if (booleanConditionBuilder == null) {
            booleanConditionBuilder = new RmsBooleanConditionBuilderImpl<>(this);
        }
        return booleanConditionBuilder;
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel1<T> getBooleanConditionBuilderWithSub1() {
        if (booleanConditionBuilderWithSub1 == null) {
            booleanConditionBuilderWithSub1 = new RmsBooleanConditionBuilderWithSub1Impl<>(this);
        }
        return booleanConditionBuilderWithSub1;
    }

    @Override
    public RmsBooleanConditionBuilderWithNestedLevel2<T> getBooleanConditionBuilderWithSub2() {
        if (booleanConditionBuilderWithSub2 == null) {
            booleanConditionBuilderWithSub2 = new RmsBooleanConditionBuilderWithSub2Impl<>(this);
        }
        return booleanConditionBuilderWithSub2;
    }

    @Override
    public ExpressionBuilderHelper getExpressionBuilderHelper() {
        return expressionBuilderHelper;
    }
}