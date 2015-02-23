package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 19/05/13
 * Time: 09:55
 * To change this template use File | Settings | File Templates.
 */
public interface RmsBaseFilterBuilder<T> {
    RmsBaseBooleanConditionBuilder<T> less(String fieldPath, Integer value);

    RmsBaseBooleanConditionBuilder<T> less(String fieldPath, Long value);

    RmsBaseBooleanConditionBuilder<T> less(String fieldPath, String value);

    RmsBaseBooleanConditionBuilder<T> greater(String fieldPath, Integer value);

    RmsBaseBooleanConditionBuilder<T> greater(String fieldPath, Long value);

    RmsBaseBooleanConditionBuilder<T> greater(String fieldPath, String value);

    RmsBaseBooleanConditionBuilder<T> lessOrEqual(String fieldPath, Integer value);

    RmsBaseBooleanConditionBuilder<T> lessOrEqual(String fieldPath, Long value);

    RmsBaseBooleanConditionBuilder<T> lessOrEqual(String fieldPath, String value);

    RmsBaseBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, Integer value);

    RmsBaseBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, Long value);

    RmsBaseBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, String value);


    RmsBaseBooleanConditionBuilder<T> equal(String fieldPath, Integer value);

    RmsBaseBooleanConditionBuilder<T> equal(String fieldPath, Long value);

    RmsBaseBooleanConditionBuilder<T> equal(String fieldPath, Boolean value);

    RmsBaseBooleanConditionBuilder<T> equal(String fieldPath, String value);

    RmsBaseBooleanConditionBuilder<T> notEqual(String fieldPath, Integer value);

    RmsBaseBooleanConditionBuilder<T> notEqual(String fieldPath, Long value);

    RmsBaseBooleanConditionBuilder<T> notEqual(String fieldPath, Boolean value);

    RmsBaseBooleanConditionBuilder<T> notEqual(String fieldPath, String value);

    RmsBaseBooleanConditionBuilder<T> in(String fieldPath, Integer... values);

    RmsBaseBooleanConditionBuilder<T> in(String fieldPath, Long... values);

    RmsBaseBooleanConditionBuilder<T> in(String fieldPath, String... values);

    RmsBaseBooleanConditionBuilder<T> notIn(String fieldPath, Integer... values);

    RmsBaseBooleanConditionBuilder<T> notIn(String fieldPath, Long... values);

    RmsBaseBooleanConditionBuilder<T> notIn(String fieldPath, String... values);

    RmsBaseBooleanConditionBuilder<T> between(String fieldPath, Integer leftBound, Integer rightBound);

    RmsBaseBooleanConditionBuilder<T> between(String fieldPath, Long leftBound, Long rightBound);

    RmsBaseBooleanConditionBuilder<T> notBetween(String fieldPath, Integer leftBound, Integer rightBound);

    RmsBaseBooleanConditionBuilder<T> notBetween(String fieldPath, Long leftBound, Long rightBound);

    RmsBaseBooleanConditionBuilder<T> exist(String fieldPath);

    RmsBaseBooleanConditionBuilder<T> notExist(String fieldPath);

    RmsBaseBooleanConditionBuilder<T> startsWith(String fieldPath, String value);
}
