/*
 * ANTLR Lexer rules declarations for QueryParser.
 */
lexer grammar QueryLexer;
@header {
package rss.commons.query.generated;
}
options {
    language=Java;
}


/* Keywords */
PLUS            : '+';
MINUS           : '-';
ADD             : 'add';
SUB             : 'sub';
MULT            : ('mul'|'*');
DIV             : ('div'|'/');
MOD             : ('mod'|'%');
EQUALS          : ('eq'|'=');
NOTEQUAL        : ('ne'|'!=');
LESS            : ('lt'|'<');
GREATER         : ('gt'|'>');
LESSOREQUALS    : ('le'|'<=');
GREATEROREQUALS : ('ge'|'>=');
DOT             : '.';
COMMA           : ',';
COLON           : ':';
RPAREN          : ')';
LPAREN          : '(';
ASC             : 'asc';
DESC            : 'desc';
BETWEEN         : 'btw';
AND             : 'and';
OR              : 'or';
NOT             : ('not'|'!');
IN              : 'in';
NULL            : 'null';
TRUE            : 'true';
FALSE           : 'false';
SQUARELPAREN    : '[';
SQUARERPAREN    : ']';
STARTS_WITH     : ('sw'|'startswith');
NOT_EXISTS      : ('notexists');

/* Analytics tokens */
OVER            : 'over';
PARTITION       : 'part';
FRAME           : 'frame';
ORDERBY         : 'orderby';

/* Analytics Frame tokens */
ROWS        :   ('R'|'r')('O'|'o')('W'|'w')('S'|'s');
RANGE       :   ('R'|'r')('A'|'a')('N'|'n')('G'|'g')('E'|'e');
UNBOUNDED   :   ('U'|'u')('N'|'n')('B'|'b')('O'|'o')('U'|'u')('N'|'n')('D'|'d')('E'|'e')('D'|'d');
PRECEDING   :   ('P'|'p')('R'|'r')('E'|'e')('C'|'c');
FOLLOWING   :   ('F'|'f')('O'|'o')('L'|'l');
CURRENTROW  :   ('C'|'c')('R'|'r')('O'|'o')('W'|'w');

/* Variable tokens (lexer configuration) */

STRING
	: '\'' (~'\'')* '\'' ( '\'' (~'\'')* '\'' )*
	;

NUMBER
	: (N? DOT)? N DOT?
	| N DOT? N? ('E'|'e') MINUS? N
	;

WHITESPACE
	: ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ -> skip
	;

fragment N
	: DIGIT+ ;

fragment DIGIT
	: '0'..'9' ;

fragment IDENTIFIER_CHAR
	: 'a'..'z' | 'A'..'Z' | '_'
	;

IDENTIFIER
    : IDENTIFIER_CHAR (IDENTIFIER_CHAR | DIGIT)*
	;
