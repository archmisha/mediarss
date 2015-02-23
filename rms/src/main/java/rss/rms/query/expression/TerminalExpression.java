package rss.rms.query.expression;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
public interface TerminalExpression extends RmsQueryExpression {
    String getFieldPath();
}
