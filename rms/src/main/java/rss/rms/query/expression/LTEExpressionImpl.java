package rss.rms.query.expression;

/**
 * An implementation of the "Less than or equal" expression
 *
 * @author Mark Bramnik
 *         Date: 13/05/13
 *         Time: 14:17
 */
class LTEExpressionImpl extends FieldAndValueSupportTerminalExpressionImpl implements LTEExpression {
    public LTEExpressionImpl(String fieldPath, Object value) {
        super(fieldPath, value);
    }
}
