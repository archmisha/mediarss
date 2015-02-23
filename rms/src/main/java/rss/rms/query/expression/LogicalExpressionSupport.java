package rss.rms.query.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 20/05/13
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */
abstract class LogicalExpressionSupport implements LogicalExpression {

    private List<RmsQueryExpression> subExpressions = new ArrayList<>();

    void addChild(RmsQueryExpression expression) {
        subExpressions.add(expression);
    }

    @Override
    public List<RmsQueryExpression> getSubExpressions() {
        return subExpressions;
    }

}
