package rss.rms.query.expression;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 13/05/13
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
abstract class FieldAndValueSupportTerminalExpressionImpl {
    protected String fieldPath;
    protected Object value;

    protected FieldAndValueSupportTerminalExpressionImpl(String fieldPath, Object value) {
        this.fieldPath = fieldPath;
        this.value = value;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public Object getValue() {
        return value;
    }
}
