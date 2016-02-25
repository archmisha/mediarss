package rss.rms.query.expression;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public interface InExpression extends TerminalExpression, NegationSupportExpression {
    public Object[] getValues();
}
