package rss.rms.query.expression;

/**
 * User: Mark Bramnik
 * Date: 09/09/13
 * Time: 11:54
 */
public class StartsWithExpressionImpl implements StartsWithExpression {
    private String fieldPath;
    private String value;

    public StartsWithExpressionImpl(String fieldPath, String value) {
        this.fieldPath = fieldPath;
        this.value = value;
    }

    @Override
    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public String getValue() {
        return value;
    }
}
