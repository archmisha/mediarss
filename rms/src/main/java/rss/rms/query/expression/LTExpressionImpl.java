package rss.rms.query.expression;

/**
 * An implementation of "Less than" expression
 *
 * @author Mark Bramnik
 *         Date: 13/05/13
 *         Time: 14:16
 */
class LTExpressionImpl extends FieldAndValueSupportTerminalExpressionImpl implements LTExpression {
    public LTExpressionImpl(String fieldPath, Object value) {
        super(fieldPath, value);
    }
}
