package rss.commons.query.api;

import org.antlr.v4.runtime.tree.TerminalNode;
import rss.commons.query.api.model.*;
import rss.commons.query.generated.QueryParser;
import rss.commons.query.generated.QueryParserBaseListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Abstract implementation of an ANTLR 4 listener for a QueryParser.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/4/13
 */
abstract class AbstractQueryParseHandler<T> extends QueryParserBaseListener {

    protected static final QueryElementFactory ELEMENT_FACTORY = new QueryElementFactory();
    protected final Stack<QueryElement> elementStack = new Stack<>();
    protected final QueryParser parser;

    /**
     * Constructs a new instance with an ANTLR generated parser.
     *
     * @param parser ANTLR generated parser.
     */
    protected AbstractQueryParseHandler(QueryParser parser) {
        this.parser = parser;
    }

    /**
     * This method calls the appropriate ANTLR 4 generated {@link QueryParser} method, to parse the relevant expression
     * and returns the appropriate tree construct.
     *
     * @return an instance of the generic type which represents a query tree construct.
     */
    protected abstract T handleParsing();

    /**
     * This method is called by the {@link QueryParserImpl} instance in charge of the current parsing process to parse
     * a query expression into a tree construct.
     *
     * @return an instance of the generic type which represents a query tree construct.
     * @see #handleParsing()
     */
    public final T parseTree() {
        this.parser.addParseListener(this);
        return handleParsing();
    }

    @Override
    public final void exitStringExpression(QueryParser.StringExpressionContext ctx) {
        elementStack.push(new StringExpression(ctx.getText()));
    }

    @Override
    public final void exitBooleanExpression(QueryParser.BooleanExpressionContext ctx) {
        final BooleanExpression stringExpression = new BooleanExpression(ctx.getText());

        elementStack.push(stringExpression);
    }

    @Override
    public final void exitFactorNumberExpression(QueryParser.FactorNumberExpressionContext ctx) {
        NumberExpression numberExpression = new NumberExpression();
        numberExpression.setValue(ctx.getText());
        elementStack.push(numberExpression);
    }

    @Override
    public final void exitPropertyExpression(QueryParser.PropertyExpressionContext ctx) {
        PropertyExpression propertyExpression = new PropertyExpression();
        List<TerminalNode> id = ctx.IDENTIFIER();

        for (TerminalNode node : id) {
            propertyExpression.addSegment(node.getText());
        }

        elementStack.push(propertyExpression);
    }

    @Override
    public final void exitVariableExpression(QueryParser.VariableExpressionContext ctx) {
        VariableExpression variableExpression = new VariableExpression(ctx.IDENTIFIER().getText());
        elementStack.push(variableExpression);
    }

    @Override
    public final void exitFunctionExpression(QueryParser.FunctionExpressionContext ctx) {
        FunctionExpression functionExpression = new FunctionExpression();

        functionExpression.setName(ctx.IDENTIFIER().getText());

        final QueryParser.AnalyticsFunctionExpressionContext analyticsFunctionExpressionContext = ctx.analyticsFunctionExpression();
        if (analyticsFunctionExpressionContext != null) {
            final AnalyticsFunctionExpression analyticsFunctionExpression = (AnalyticsFunctionExpression) elementStack.pop();
            functionExpression.setAnalyticsFunctionExpression(analyticsFunctionExpression);
        }

        QueryParser.ExpressionListContext expressionListContext = ctx.expressionList();
        if (expressionListContext != null) {
            int parameterCount = expressionListContext.expr().size();
            LinkedList<Expression> parameters = new LinkedList<>();
            for (int i = 0; i < parameterCount; i++) {
                // Always add first to reverse the stack order
                parameters.addFirst((Expression) elementStack.pop());
            }

            functionExpression.addParameters(parameters);
        }

        elementStack.push(functionExpression);
    }

    @Override
    public void exitFactorUnaryExpression(QueryParser.FactorUnaryExpressionContext ctx) {
        if (ctx.plusMinusSign().MINUS() != null) {
            UnaryExpression unaryExpression = new UnaryExpression();
            unaryExpression.setExpression((Expression) elementStack.pop());
            unaryExpression.setOperator(UnaryExpression.Operator.MINUS);

            elementStack.push(unaryExpression);
        } else if (ctx.plusMinusSign().PLUS() != null) {
            UnaryExpression unaryExpression = new UnaryExpression();
            unaryExpression.setExpression((Expression) elementStack.pop());
            unaryExpression.setOperator(UnaryExpression.Operator.PLUS);

            elementStack.push(unaryExpression);
        }
    }

    @Override
    public void exitTerm(QueryParser.TermContext ctx) {
        if (!ctx.MULT().isEmpty()) {
            handleBinaryExpression(ctx.MULT().size(), BinaryExpression.Operator.MULTIPLY);
        } else if (!ctx.DIV().isEmpty()) {
            handleBinaryExpression(ctx.DIV().size(), BinaryExpression.Operator.DIVIDE);
        } else if (!ctx.MOD().isEmpty()) {
            handleBinaryExpression(ctx.MOD().size(), BinaryExpression.Operator.MODULO);
        }
    }

    @Override
    public final void exitExpr(QueryParser.ExprContext ctx) {
        if (!ctx.MINUS().isEmpty()) {
            handleBinaryExpression(ctx.MINUS().size(), BinaryExpression.Operator.SUB);
        } else if (!ctx.SUB().isEmpty()) {
            handleBinaryExpression(ctx.SUB().size(), BinaryExpression.Operator.SUB);
        } else if (!ctx.PLUS().isEmpty()) {
            handleBinaryExpression(ctx.PLUS().size(), BinaryExpression.Operator.ADD);
        } else if (!ctx.ADD().isEmpty()) {
            handleBinaryExpression(ctx.ADD().size(), BinaryExpression.Operator.ADD);
        }

    }


    @Override
    public void exitWrappedExpression(QueryParser.WrappedExpressionContext ctx) {
        WrappedExpression wrappedExpression = new WrappedExpression();
        wrappedExpression.setExpression((Expression) elementStack.pop());
        elementStack.push(wrappedExpression);
    }

    /**
     * If operatorCount is greater than 1 than this method is going to create a binary expression tree.
     * <p/>
     * For example, if the input expression if '1 + 2 + 3', the expression which will be on the top of the stack
     * after this method is called id the following:
     * <p/>
     * <pre>
     *      BinaryExpression {
     *          Operator        =   '+'
     *          LeftExpression  =   '1'
     *          RightExpression =   BinaryExpression {
     *                                  Operator        = '+'
     *                                  LeftExpression  = '2'
     *                                  RightExpression = '3'
     *                              }
     *      }
     * </pre>
     *
     * @param operatorCount number of operator occurrences.
     * @param operator      the binary operator.
     */
    private void handleBinaryExpression(int operatorCount, BinaryExpression.Operator operator) {

        for (int i = 0; i < operatorCount; i++) {
            BinaryExpression binaryExpr = new BinaryExpression();

            binaryExpr.setOperator(operator);
            // Pop two expressions from the stack and assign them is reverse order - right first.
            binaryExpr.setRightExpression((Expression) elementStack.pop());
            binaryExpr.setLeftExpression((Expression) elementStack.pop());

            // Push the new binary expression into the stack for further handling.
            elementStack.push(binaryExpr);
        }
    }

}
