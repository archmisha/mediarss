package rss.rms.query.expression;

/**
 * An implementation of the "Greater of equal" expression
 *
 * @author Mark Bramnik
 *         Date: 13/05/13
 *         Time: 14:15
 */
class GTEExpressionImpl extends FieldAndValueSupportTerminalExpressionImpl implements GTEExpression {
    public GTEExpressionImpl(String fieldPath, Object value) {
        super(fieldPath, value);
    }
}
