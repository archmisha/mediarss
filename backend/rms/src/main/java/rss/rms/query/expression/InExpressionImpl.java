package rss.rms.query.expression;

/**
 * An implementation of the "in/not in" expression
 * Date: 13/05/13
 * Time: 14:20
 */
class InExpressionImpl extends FieldAndValueSupportTerminalExpressionImpl implements InExpression {
    private boolean negation;

    public InExpressionImpl(String fieldPath, boolean negation, Object... value) {
        super(fieldPath, value);
        this.negation = negation;
    }

    @Override
    public boolean hasNegation() {
        return negation;
    }

    @Override
    public Object[] getValues() {
        return (Object[]) value;
    }
}

