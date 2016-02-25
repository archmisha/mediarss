package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */
public interface RmsBooleanConditionBuilderWithNestedLevel1<T> extends RmsBaseBooleanConditionBuilder<T>, RmsCloseParenSupportBuilder<T> {
    RmsFilterBuilderWithNestedLevel1<T> and();

    RmsFilterBuilderWithNestedLevel1<T> or();

    RmsBooleanConditionBuilder<T> closeParen();
}
