package rss.commons.query.api.model;

import java.util.regex.Pattern;

import static rss.commons.query.api.model.SyntaxConst.QUOTE;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class StringExpression implements Expression {

    // from ANTLR rule: '\'' (~'\'')* '\'' ( '\'' (~'\'')* '\'' )*
    private static final Pattern STRING_PATTERN = Pattern.compile(
            "'[^']*'('[^']*')*",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /**
     * @param literal a string literal to validate.
     * @return <code>true</code> if the specified literal is valid.
     */
    private static boolean isValidLiteral(String literal) {
        return STRING_PATTERN.matcher(literal).matches();
    }

    private static String unparse(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(QUOTE);
        sb.append(str.replace("'", "''"));
        sb.append(QUOTE);
        return sb.toString();
    }

    private String value;

    public StringExpression() {
    }

    /**
     * Constructs from literal representation
     *
     * @param literal a String literal
     */
    public StringExpression(String literal) {
        setLiteral(literal);
    }

    /**
     * @return the value of this expression.
     */
    public final String getValue() {
        return value;
    }

    /**
     * @param value a value to set.
     */
    public final void setValue(String value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        String str = "null";
        if (value != null) {
            str = unparse(value);
        }

        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return toApiString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        visitor.exit(this, context);
    }

    /**
     * @param literal a string literal to set.
     */
    private void setLiteral(String literal) {
        if (literal != null) {
            if (literal.length() == 0) {
                throw new IllegalArgumentException();
            }
            if (!isValidLiteral(literal)) {
                throw new IllegalArgumentException();
            }
            /* Enforced by literal validation pattern
             * if (literal.length() < 2) {
             *      throw new IllegalArgumentException();
             * }
             */
            if ((literal.charAt(0) == QUOTE)
                    && (literal.charAt(literal.length() - 1) == QUOTE)) {
                String s = literal.substring(1, literal.length() - 1);
                /* Enforced by literal validation pattern
                 * if (s.replace("''", "").contains("'")) {
                 *    throw new IllegalArgumentException(); // string contained single apostrophe
                 * }
                 */
                value = s.replace("''", "'");
            }

        }
    }
}


