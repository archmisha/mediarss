package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 19/05/13
 * Time: 09:56
 * To change this template use File | Settings | File Templates.
 */
public interface RmsBaseBooleanConditionBuilder<T> {
    RmsBaseFilterBuilder<T> and();

    RmsBaseFilterBuilder<T> or();
}
