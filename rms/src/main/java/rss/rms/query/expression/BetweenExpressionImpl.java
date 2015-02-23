package rss.rms.query.expression;

/**
 * An implementation of the Between expression
 *
 * @author Mark Bramnik
 */
class BetweenExpressionImpl implements BetweenExpression {
    private String fieldPath;
    private Object leftBoundValue;
    private Object rightBoundValue;
    private boolean negation;

    public BetweenExpressionImpl(String fieldPath, Object leftBoundValue, Object rightBoundValue, boolean negation) {
        this.fieldPath = fieldPath;
        this.leftBoundValue = leftBoundValue;
        this.rightBoundValue = rightBoundValue;
        this.negation = negation;
    }

    @Override
    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public Object getLeftBoundValue() {
        return leftBoundValue;
    }

    @Override
    public Object getRightBoundValue() {
        return rightBoundValue;
    }

    @Override
    public boolean hasNegation() {
        return negation;
    }
}
