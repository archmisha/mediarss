package rss.rms.query.builder;

/**
 * @author Mark Bramnik
 *         Date: 3/21/13
 *         Time: 3:57 PM
 * @since 1.0.0-9999
 */
public interface RmsFilterBuilder<T> extends RmsBaseFilterBuilder<T>, RmsOpenParenSupportBuilder<T> {

    RmsBooleanConditionBuilder<T> less(String fieldPath, Integer value);

    RmsBooleanConditionBuilder<T> less(String fieldPath, Long value);

    RmsBooleanConditionBuilder<T> less(String fieldPath, String value);

    RmsBooleanConditionBuilder<T> greater(String fieldPath, Integer value);

    RmsBooleanConditionBuilder<T> greater(String fieldPath, Long value);

    RmsBooleanConditionBuilder<T> greater(String fieldPath, String value);

    RmsBooleanConditionBuilder<T> lessOrEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilder<T> lessOrEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilder<T> lessOrEqual(String fieldPath, String value);

    RmsBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilder<T> greaterOrEqual(String fieldPath, String value);

    RmsBooleanConditionBuilder<T> equal(String fieldPath, Integer value);

    RmsBooleanConditionBuilder<T> equal(String fieldPath, Long value);

    RmsBooleanConditionBuilder<T> equal(String fieldPath, Boolean value);

    RmsBooleanConditionBuilder<T> equal(String fieldPath, String value);

    RmsBooleanConditionBuilder<T> notEqual(String fieldPath, Integer value);

    RmsBooleanConditionBuilder<T> notEqual(String fieldPath, Long value);

    RmsBooleanConditionBuilder<T> notEqual(String fieldPath, Boolean value);

    RmsBooleanConditionBuilder<T> notEqual(String fieldPath, String value);


    RmsBooleanConditionBuilder<T> in(String fieldPath, Integer... values);

    RmsBooleanConditionBuilder<T> in(String fieldPath, Long... values);

    RmsBooleanConditionBuilder<T> in(String fieldPath, String... values);

    RmsBooleanConditionBuilder<T> notIn(String fieldPath, Integer... values);

    RmsBooleanConditionBuilder<T> notIn(String fieldPath, Long... values);

    RmsBooleanConditionBuilder<T> notIn(String fieldPath, String... values);

    RmsBooleanConditionBuilder<T> between(String fieldPath, Integer leftBound, Integer rightBound);

    RmsBooleanConditionBuilder<T> between(String fieldPath, Long leftBound, Long rightBound);

    RmsBooleanConditionBuilder<T> notBetween(String fieldPath, Integer leftBound, Integer rightBound);

    RmsBooleanConditionBuilder<T> notBetween(String fieldPath, Long leftBound, Long rightBound);

    RmsBooleanConditionBuilder<T> exist(String fieldPath);

    RmsBooleanConditionBuilder<T> notExist(String fieldPath);

    RmsBooleanConditionBuilder<T> startsWith(String fieldPath, String value);

    RmsFilterBuilderWithNestedLevel1<T> openParen();
    //RmsQueryBuilder<T> done();
}
