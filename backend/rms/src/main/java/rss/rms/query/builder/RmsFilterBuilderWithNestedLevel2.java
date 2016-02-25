package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 19:55
 * To change this template use File | Settings | File Templates.
 */
public interface RmsFilterBuilderWithNestedLevel2<T> extends RmsBaseFilterBuilder<T> {
    RmsBooleanConditionBuilderWithNestedLevel2<T> less(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> less(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> greater(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> greater(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> lessOrEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> lessOrEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> greaterOrEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> greaterOrEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> equal(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> equal(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> equal(String fieldPath, Boolean value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> equal(String fieldPath, String value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notEqual(String fieldPath, Boolean value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notEqual(String fieldPath, String value);

    RmsBooleanConditionBuilderWithNestedLevel2<T> in(String fieldPath, Integer... values);

    RmsBooleanConditionBuilderWithNestedLevel2<T> in(String fieldPath, Long... values);

    RmsBooleanConditionBuilderWithNestedLevel2<T> in(String fieldPath, String... values);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notIn(String fieldPath, Integer... values);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notIn(String fieldPath, Long... values);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notIn(String fieldPath, String... values);

    RmsBooleanConditionBuilderWithNestedLevel2<T> between(String fieldPath, Integer leftBound, Integer rightBound);

    RmsBooleanConditionBuilderWithNestedLevel2<T> between(String fieldPath, Long leftBound, Long rightBound);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notBetween(String fieldPath, Integer leftBound, Integer rightBound);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notBetween(String fieldPath, Long leftBound, Long rightBound);

    RmsBooleanConditionBuilderWithNestedLevel2<T> exist(String fieldPath);

    RmsBooleanConditionBuilderWithNestedLevel2<T> notExist(String fieldPath);

    RmsBooleanConditionBuilderWithNestedLevel2<T> startsWith(String fieldPath, String value);
}
