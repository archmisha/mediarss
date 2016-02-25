package rss.commons.query.api;

import rss.commons.query.api.model.*;
import rss.commons.query.generated.QueryParser;

import java.util.LinkedList;

import static rss.commons.query.api.model.SyntaxConst.*;

/**
 * This class implements a {@link com.hp.maas.platform.commons.query.generated.QueryParserListener} (generated ANTLR
 * 4 interface) and used for parsing of {@link com.hp.maas.platform.commons.query.api.model.QueryFilter} constructs.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/4/13
 */
class QueryFilterParseHandler extends AbstractQueryParseHandler<QueryFilter> {

    private final QueryFilter queryFilter = ELEMENT_FACTORY.emptyFilter();

    protected QueryFilterParseHandler(QueryParser parser) {
        super(parser);
    }

    @Override
    public void exitInCondition(QueryParser.InConditionContext ctx) {
        InCondition inCondition = new InCondition();

        if (ctx.NOT() != null) {
            inCondition.setNot(true);
        }

        int parameterCount = ctx.expressionList().expr().size();
        LinkedList<Expression> parameters = new LinkedList<>();
        for (int i = 0; i < parameterCount; i++) {
            // Always add first to reverse the stack order
            parameters.addFirst((Expression) elementStack.pop());
        }

        inCondition.addExpressions(parameters);
        inCondition.setExpression((Expression) elementStack.pop());

        elementStack.push(inCondition);
    }

    @Override
    public void exitBetweenCondition(rss.commons.query.generated.QueryParser.BetweenConditionContext ctx) {
        BetweenCondition betweenCondition = new BetweenCondition();

        if (ctx.NOT() != null) {
            betweenCondition.setNot(true);
        }

        betweenCondition.setRightBoundary((Expression) elementStack.pop());
        betweenCondition.setLeftBoundary((Expression) elementStack.pop());
        betweenCondition.setExpression((Expression) elementStack.pop());

        elementStack.push(betweenCondition);
    }

    @Override
    public void exitConditionAnd(QueryParser.ConditionAndContext ctx) {

        // If the tree contains an AND node, we need to create a LogicalCondition here
        if (!ctx.AND().isEmpty()) {
            LogicalCondition logicalCondition = new LogicalCondition();
            logicalCondition.setOperator(LogicalCondition.Operator.AND);

            int conditionCount = ctx.simpleCondition().size();
            LinkedList<Condition> conditions = new LinkedList<>();
            for (int i = 0; i < conditionCount; i++) {
                // Always add first to reverse the stack order
                conditions.addFirst((Condition) elementStack.pop());
            }

            logicalCondition.addConditions(conditions);
            elementStack.push(logicalCondition);
        }
    }

    @Override
    public void exitSimpleCondition(QueryParser.SimpleConditionContext ctx) {
        if (ctx.LPAREN() != null) {
            WrappedCondition wrappedCondition = new WrappedCondition();
            wrappedCondition.setCondition((Condition) elementStack.pop());
            elementStack.push(wrappedCondition);
        }
    }

    @Override
    public void exitSimpleComparisonCondition(QueryParser.SimpleComparisonConditionContext ctx) {
        ComparisonCondition comparisonCondition = new ComparisonCondition();

        comparisonCondition.setOperator(getComparisonOperator(ctx.comparisonOperator().getText().toLowerCase()));
        comparisonCondition.setRightExpression((Expression) elementStack.pop());
        comparisonCondition.setLeftExpression((Expression) elementStack.pop());

        elementStack.push(comparisonCondition);
    }

    @Override
    public void exitStartsWithCondition(QueryParser.StartsWithConditionContext ctx) {
        StartsWithCondition startsWithCondition = new StartsWithCondition();
        startsWithCondition.setPattern((Expression) elementStack.pop());
        startsWithCondition.setExpression((Expression) elementStack.pop());
        startsWithCondition.setNot(ctx.NOT() != null);
        elementStack.push(startsWithCondition);
    }

    @Override
    public void exitNullCondition(QueryParser.NullConditionContext ctx) {
        NullCondition nullCondition = new NullCondition();
        nullCondition.setExpression((Expression) elementStack.pop());
        nullCondition.setNot(ctx.NOTEQUAL() != null);
        elementStack.push(nullCondition);
    }

    @Override
    public void exitVectorCondition(QueryParser.VectorConditionContext ctx) {
        VectorCondition vectorCondition = new VectorCondition();
        vectorCondition.setVectorName(ctx.IDENTIFIER().getText());
        if (ctx.condition() != null) {
            vectorCondition.setCondition((Condition) elementStack.pop());
        } else { //support empty vector
            ComparisonCondition emptyCondition = new ComparisonCondition();
            NumberExpression numberExpression = new NumberExpression("0");
            PropertyExpression propertyExpression = new PropertyExpression();
            propertyExpression.addSegment("Id");
            emptyCondition.setLeftExpression(propertyExpression);
            emptyCondition.setRightExpression(numberExpression);
            emptyCondition.setOperator(ComparisonCondition.Operator.GREATER);
            vectorCondition.setCondition(emptyCondition);
        }
        vectorCondition.setNotexists(ctx.NOT_EXISTS() != null);
        elementStack.push(vectorCondition);
    }

    @Override
    public void exitCondition(QueryParser.ConditionContext ctx) {

        // If the tree contains an OR node, we need to create a LogicalCondition here
        if (!ctx.OR().isEmpty()) {
            LogicalCondition logicalCondition = new LogicalCondition();
            logicalCondition.setOperator(LogicalCondition.Operator.OR);

            int conditionCount = ctx.conditionAnd().size();
            LinkedList<Condition> conditions = new LinkedList<>();
            for (int i = 0; i < conditionCount; i++) {
                // Always add first to reverse the stack order
                conditions.addFirst((Condition) elementStack.pop());
            }

            logicalCondition.addConditions(conditions);
            elementStack.push(logicalCondition);
        }
    }

    @Override
    public void exitQueryFilter(QueryParser.QueryFilterContext ctx) {
        queryFilter.setCondition((Condition) elementStack.pop());
    }

    @Override
    protected QueryFilter handleParsing() {
        parser.queryFilter();
        return queryFilter;
    }

    private ComparisonCondition.Operator getComparisonOperator(String parsedOperatorString) {
        switch (parsedOperatorString) {
            case OP_EQUAL:
                return ComparisonCondition.Operator.EQUALS;

            case OP_NOT_EQUAL:
                return ComparisonCondition.Operator.NOTEQUAL;

            case OP_LESS:
                return ComparisonCondition.Operator.LESS;

            case OP_GREATER:
                return ComparisonCondition.Operator.GREATER;

            case OP_LESS_OR_EQUAL:
                return ComparisonCondition.Operator.LESSOREQUALS;

            case OP_GREATER_OR_EQUAL:
                return ComparisonCondition.Operator.GREATEROREQUALS;

            case OP_VERBOSE_EQUAL:
                return ComparisonCondition.Operator.EQUALS;

            case OP_VERBOSE_NOT_EQUAL:
                return ComparisonCondition.Operator.NOTEQUAL;

            case OP_VERBOSE_LESS:
                return ComparisonCondition.Operator.LESS;

            case OP_VERBOSE_GREATER:
                return ComparisonCondition.Operator.GREATER;

            case OP_VERBOSE_LESS_OR_EQUAL:
                return ComparisonCondition.Operator.LESSOREQUALS;

            case OP_VERBOSE_GREATER_OR_EQUAL:
                return ComparisonCondition.Operator.GREATEROREQUALS;

            default:
                throw new QueryParserException(parsedOperatorString + " is not a valid comparison operator. This is a bug!");
        }
    }
}
