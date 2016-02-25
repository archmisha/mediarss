package rss.rms.query.expression;

/**
 * An implementation of the "Equals" expression
 *
 * @author Mark Bramnik
 */
class EqExpressionImpl extends FieldAndValueSupportTerminalExpressionImpl implements EqExpression {

    public EqExpressionImpl(String fieldPath, Object value) {
        super(fieldPath, value);
    }
}
