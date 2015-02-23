package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 10/05/13
 * Time: 06:25
 * To change this template use File | Settings | File Templates.
 */
public interface RmsBooleanConditionBuilder<T> extends RmsBaseBooleanConditionBuilder<T> {
    RmsFilterBuilder<T> and();

    RmsFilterBuilder<T> or();

    RmsQueryBuilder<T> done();
}
