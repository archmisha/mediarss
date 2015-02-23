package rss.rms.query.builder;

import rss.rms.query.expression.ExpressionBuilderHelper;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 22/05/13
 * Time: 09:22
 * To change this template use File | Settings | File Templates.
 */
public interface FilterBuilderContextManager<T> {

    ModifiableRmsQueryBuilder<T> getQueryBuilder();

    RmsFilterBuilder<T> getFilterBuilder();

    RmsFilterBuilderWithNestedLevel1<T> getFilterBuilderWithSub1();

    RmsFilterBuilderWithNestedLevel2<T> getFilterBuilderWithSub2();

    RmsBooleanConditionBuilder<T> getBooleanConditionBuilder();

    RmsBooleanConditionBuilderWithNestedLevel1<T> getBooleanConditionBuilderWithSub1();

    RmsBooleanConditionBuilderWithNestedLevel2<T> getBooleanConditionBuilderWithSub2();

    ExpressionBuilderHelper getExpressionBuilderHelper();

}
