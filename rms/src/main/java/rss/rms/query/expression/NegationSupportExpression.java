package rss.rms.query.expression;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 13/05/13
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
public interface NegationSupportExpression extends RmsQueryExpression {
    boolean hasNegation();
}
