package rss.rms.query.expression;

/**
 * An implementation of the "Exists" expression
 *
 * @author Mark Bramnik
 *         Date: 13/05/13
 *         Time: 14:31
 */
class ExistsExpressionImpl implements ExistsExpression {
    private String fieldPath;
    private boolean negation;

    public ExistsExpressionImpl(String fieldPath, boolean negation) {
        this.fieldPath = fieldPath;
        this.negation = negation;
    }

    @Override
    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public boolean hasNegation() {
        return negation;
    }
}
