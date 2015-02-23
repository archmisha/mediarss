package rss.rms.query.expression;

/**
 * An implementation of Not Equals expression
 *
 * @author Mark Bramnik
 *         Date: 20/05/13
 *         Time: 15:41
 */
class NotEqExpressionImpl extends FieldAndValueSupportTerminalExpressionImpl implements NotEqExpression {
    public NotEqExpressionImpl(String fieldPath, Object value) {
        super(fieldPath, value);
    }
}
