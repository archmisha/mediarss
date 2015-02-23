/**
 *
 */
package rss.commons.query.api.model;


/**
 * This interface declares syntax keywords and other character representations of operators and seperators.
 *
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public interface SyntaxConst {

	/*
     * *** KEYWORDS REFERENCE ***
	 */

    /***/
    String KW_OR = "or";
    /***/
    String KW_AND = "and";
    /***/
    String KW_BETWEEN = "btw";
    /***/
    String KW_IN = "in";
    /***/
    String KW_ASC = "asc";
    /***/
    String KW_DESC = "desc";
    /***/
    String KW_STARTSWITH = "sw";
    /***/
    String KW_NULL = "null";

    /**
     * analytical *
     */
    String KW_OVER = "over";

	/* OPERATOR REFERENCE */

    /***/
    String OP_PLUS = "+";
    /***/
    String OP_MINUS = "-";
    /***/
    String OP_VERBOSE_ADD = "add";
    /***/
    String OP_VERBOSE_SUB = "sub";
    /***/
    String OP_VERBOSE_MULTIPLY = "mul";
    /***/
    String OP_VERBOSE_DIVIDE = "div";
    /***/
    String OP_VERBOSE_MODULO = "mod";
    /***/
    String OP_VERBOSE_EQUAL = "eq";
    /***/
    String OP_VERBOSE_NOT_EQUAL = "ne";
    /***/
    String OP_VERBOSE_LESS = "lt";
    /***/
    String OP_VERBOSE_GREATER = "gt";
    /***/
    String OP_VERBOSE_LESS_OR_EQUAL = "le";
    /***/
    String OP_VERBOSE_GREATER_OR_EQUAL = "ge";

    /***/
    String OP_ADD = "+";
    /***/
    String OP_SUB = "-";
    /***/
    String OP_MULTIPLY = "*";
    /***/
    String OP_DIVIDE = "/";
    /***/
    String OP_MODULO = "%";
    /***/
    String OP_EQUAL = "=";
    /***/
    String OP_NOT_EQUAL = "!=";
    /***/
    String OP_LESS = "<";
    /***/
    String OP_GREATER = ">";
    /***/
    String OP_LESS_OR_EQUAL = "<=";
    /***/
    String OP_GREATER_OR_EQUAL = ">=";
    /***/
    String OP_NOT = "!";


	/* OTHER CHARACTERS */

    /***/
    char SPACE = ' ';
    /***/
    char QUOTE = '\'';
    /***/
    char LPAREN = '(';
    /***/
    char RPAREN = ')';
    /***/
    char DOT = '.';
    /***/
    char COMMA = ',';
    /***/
    char COLON = ':';
    /***/
    char SQUARELPAREN = '[';
    /***/
    char SQUARERPAREN = ']';
    /***/
    String SP_COMMA = ", ";

    String VECTOR_NOTEXISTS = "notexists ";
}
