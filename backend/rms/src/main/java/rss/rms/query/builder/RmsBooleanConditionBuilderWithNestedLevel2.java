package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 20:02
 * To change this template use File | Settings | File Templates.
 */
public interface RmsBooleanConditionBuilderWithNestedLevel2<T> extends RmsBaseBooleanConditionBuilder<T>, RmsCloseParenSupportBuilder<T> {
    RmsFilterBuilderWithNestedLevel2<T> and();

    RmsFilterBuilderWithNestedLevel2<T> or();

    RmsBooleanConditionBuilderWithNestedLevel1<T> closeParen();
}
