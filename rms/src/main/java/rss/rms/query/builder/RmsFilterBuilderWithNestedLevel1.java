package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 19:43
 * To change this template use File | Settings | File Templates.
 */
public interface RmsFilterBuilderWithNestedLevel1<T> extends RmsBaseFilterBuilder<T>, RmsOpenParenSupportBuilder<T> {
    RmsBooleanConditionBuilderWithNestedLevel1<T> less(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> less(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> greater(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> greater(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> lessOrEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> lessOrEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> greaterOrEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> greaterOrEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> equal(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> equal(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> equal(String fieldPath, Boolean value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> equal(String fieldPath, String value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notEqual(String fieldPath, Boolean value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notEqual(String fieldPath, String value);

    RmsBooleanConditionBuilderWithNestedLevel1<T> in(String fieldPath, Integer... values);

    RmsBooleanConditionBuilderWithNestedLevel1<T> in(String fieldPath, Long... values);

    RmsBooleanConditionBuilderWithNestedLevel1<T> in(String fieldPath, String... values);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notIn(String fieldPath, Integer... values);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notIn(String fieldPath, Long... values);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notIn(String fieldPath, String... values);

    RmsBooleanConditionBuilderWithNestedLevel1<T> between(String fieldPath, Integer leftBound, Integer rightBound);

    RmsBooleanConditionBuilderWithNestedLevel1<T> between(String fieldPath, Long leftBound, Long rightBound);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notBetween(String fieldPath, Integer leftBound, Integer rightBound);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notBetween(String fieldPath, Long leftBound, Long rightBound);

    RmsBooleanConditionBuilderWithNestedLevel1<T> exist(String fieldPath);

    RmsBooleanConditionBuilderWithNestedLevel1<T> notExist(String fieldPath);

    RmsBooleanConditionBuilderWithNestedLevel1<T> startsWith(String fieldPath, String value);

    RmsFilterBuilderWithNestedLevel2<T> openParen();


}
