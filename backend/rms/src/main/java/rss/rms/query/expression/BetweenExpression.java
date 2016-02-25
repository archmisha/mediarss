package rss.rms.query.expression;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public interface BetweenExpression extends TerminalExpression, NegationSupportExpression {
    Object getLeftBoundValue();

    Object getRightBoundValue();
}
