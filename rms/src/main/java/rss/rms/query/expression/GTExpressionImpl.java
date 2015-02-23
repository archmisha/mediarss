package rss.rms.query.expression;

/**
 * An implementation of the "Greater than" expression
 *
 * @author Mark Bramnik
 *         Date: 13/05/13
 *         Time: 14:15
 */
class GTExpressionImpl extends FieldAndValueSupportTerminalExpressionImpl implements GTExpression {
    public GTExpressionImpl(String fieldPath, Object value) {
        super(fieldPath, value);
    }
}
