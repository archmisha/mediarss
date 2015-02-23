package rss.rms.query;

import rss.rms.query.expression.RmsQueryExpression;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class FilterInformationImpl implements FilterInformation {

    private RmsQueryExpression expression;

    public FilterInformationImpl(RmsQueryExpression expression) {
        this.expression = expression;
    }

    @Override
    public RmsQueryExpression getExpression() {
        return expression;
    }
}
