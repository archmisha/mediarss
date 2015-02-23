package rss.rms.query.expression;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 13/05/13
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public interface LogicalExpression extends RmsQueryExpression {
    List<RmsQueryExpression> getSubExpressions();
}
