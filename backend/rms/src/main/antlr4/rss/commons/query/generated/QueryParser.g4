/*
 * ANTLR Parsing rules for QueryParser.
 */
parser grammar QueryParser;

options {
    language=Java;
    tokenVocab=QueryLexer;
}
   
@header {
package rss.commons.query.generated;

import rss.commons.query.api.QueryParserException;

}
@parser::members {

/**
 * This method overrides the default exitRule implementation of ANTLR in order to immediately exit with a
 * QueryParserException when the context contains errors.
 */
@Override
public void exitRule() {

    RecognitionException e = getContext().exception;
    if (e != null) {
        final String devMessagePattern = "Unexpected token: %s at %s";
        String tokenErrorDisplay = getTokenErrorDisplay(e.getOffendingToken());
        String errorHeader = getErrorHeader(e);
        throw new QueryParserException(String.format(devMessagePattern, tokenErrorDisplay, errorHeader), e);
    }

    super.exitRule();
}
}

// *** Root Rules

queryMeta
    : propertyExpression (COMMA propertyExpression)*
    ;

queryLayout
	: expressionList EOF
	;

queryFilter
	: condition EOF
	;

queryOrder
    : orderList EOF
    ;

queryGroupBy
    : expressionList EOF
    ;

queryPage
    : NUMBER COMMA NUMBER EOF
    ;

// *** Expression Rules

expr
	: term ( ( ADD | PLUS | SUB | MINUS )  term )*
	;

term
	: factor ( ( MULT | DIV | MOD ) factor )*
	;

factor
	: wrappedExpression     #factorWrappedExpression
	| plusMinusSign expr    #factorUnaryExpression
	| NUMBER                #factorNumberExpression
	| stringExpression      #factorStringExpression
	| booleanExpression     #factorBooleanExpression
	| propertyExpression    #factorPropertyExpression
	| variableExpression    #factorVariableExpression
	| functionExpression    #factorFunctionExpression
    ;

plusMinusSign
	: PLUS
	| MINUS
	;

wrappedExpression
    : LPAREN expr RPAREN
    ;

stringExpression
    : STRING
    ;

booleanExpression
    : TRUE
    | FALSE
    ;

propertyExpression
    : IDENTIFIER (DOT IDENTIFIER)*
    ;

expressionList
	: expr (COMMA expr)*
	;

variableExpression
	:  COLON IDENTIFIER
	;

functionExpression
    : IDENTIFIER LPAREN expressionList? RPAREN analyticsFunctionExpression?
    ;

// *** Analytics Rules

analyticsFunctionExpression
    : OVER analyticsPartitionExpression? analyticsOrderExpression? analyticsFrameElement?
    ;

analyticsPartitionExpression
    : PARTITION LPAREN propertyExpression RPAREN
    ;

analyticsOrderExpression
    : ORDERBY LPAREN orderList RPAREN
    ;

analyticsFrameElement
    : FRAME LPAREN frameClause RPAREN
    ;

frameClause
    : (rangeRows frameStart)      #frameStartOnly
    | (rangeRows BETWEEN frameStart AND frameEnd) #frameStartEnd
    ;

rangeRows
    : (ROWS | RANGE)
    ;

frameStart
    : (UNBOUNDED PRECEDING)
    | (NUMBER PRECEDING)
    | (CURRENTROW)
    | (NUMBER FOLLOWING)
    ;

frameEnd
    : (NUMBER PRECEDING)
    | (CURRENTROW)
    | (NUMBER FOLLOWING)
    | (UNBOUNDED FOLLOWING)
    ;

// *** Condition Rules

condition
	: conditionAnd (OR conditionAnd)*
	;

conditionAnd
	: simpleCondition (AND simpleCondition)*
	;

simpleCondition
	: (LPAREN condition RPAREN)
	| simpleComparisonCondition
	| betweenCondition
	| inCondition
	| startsWithCondition
	| vectorCondition
	| nullCondition
	;

simpleComparisonCondition
	: expr comparisonOperator expr
	;

startsWithCondition
    : e=expr NOT? STARTS_WITH LPAREN bound1=expr RPAREN
    ;

comparisonOperator
	: EQUALS | NOTEQUAL | LESS | GREATER | LESSOREQUALS | GREATEROREQUALS
	;

betweenCondition
  : e=expr NOT? BETWEEN LPAREN bound1=expr COMMA bound2=expr RPAREN
  ;

inCondition
    : expr NOT? IN LPAREN expressionList RPAREN
    ;

nullCondition
    : expr (EQUALS | NOTEQUAL) NULL
    ;

vectorCondition
    : NOT_EXISTS? IDENTIFIER SQUARELPAREN condition? SQUARERPAREN
    ;

// *** Order Rules
orderList
	: orderItem (COMMA orderItem)*
	;

orderItem
	: expr (ASC | DESC)?
	;