package rss.commons.query.api;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;
import rss.commons.query.api.model.*;
import rss.commons.query.generated.QueryParser;

/**
 * This class implements a {@link com.hp.maas.platform.commons.query.generated.QueryParserListener} (generated ANTLR
 * 4 interface) and used for parsing of {@link com.hp.maas.platform.commons.query.api.model.QueryLayout} constructs.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/4/13
 */
class QueryLayoutParseHandler extends AbstractQueryParseHandler<QueryLayout> {

    private final QueryLayout queryLayout = ELEMENT_FACTORY.emptyLayout();

    protected QueryLayoutParseHandler(QueryParser parser) {
        super(parser);
    }

    @Override
    public void exitAnalyticsFunctionExpression(@NotNull QueryParser.AnalyticsFunctionExpressionContext ctx) {
        final AnalyticsFunctionExpression analyticsFunctionExpression = new AnalyticsFunctionExpression();

        final QueryParser.AnalyticsFrameElementContext analyticsFrameElementContext = ctx.analyticsFrameElement();
        if (analyticsFrameElementContext != null) {
            final AnalyticsFrameElement analyticsFrameElement = (AnalyticsFrameElement) elementStack.pop();
            analyticsFunctionExpression.setFrameElement(analyticsFrameElement);
        }

        final QueryParser.AnalyticsOrderExpressionContext analyticsOrderExpressionContext = ctx.analyticsOrderExpression();
        if (analyticsOrderExpressionContext != null) {
            boolean hasOrderByExpression = true;
            while (hasOrderByExpression) {
                final QueryElement peek = elementStack.peek();
                if (peek instanceof OrderExpression) {
                    final OrderExpression orderExpression = (OrderExpression) elementStack.pop();
                    analyticsFunctionExpression.addOrderExpressions(orderExpression);
                } else {
                    hasOrderByExpression = false;
                }
            }
        }

        final QueryParser.AnalyticsPartitionExpressionContext analyticsPartitionExpressionContext = ctx.analyticsPartitionExpression();
        if (analyticsPartitionExpressionContext != null) {
            final Expression partitionExpression = (Expression) elementStack.pop();
            analyticsFunctionExpression.setPartitionExpression(partitionExpression);
        }

        elementStack.push(analyticsFunctionExpression);
    }

    @Override
    public void exitOrderItem(QueryParser.OrderItemContext ctx) {
        OrderExpression orderExpression = new OrderExpressionImpl();
        if (ctx.ASC() != null) {
            orderExpression.setDirection(OrderExpression.Direction.ASC);
        } else if (ctx.DESC() != null) {
            orderExpression.setDirection(OrderExpression.Direction.DESC);
        }

        orderExpression.setExpression((Expression) elementStack.pop());
        elementStack.push(orderExpression);
    }

    @Override
    public void exitRangeRows(QueryParser.RangeRowsContext ctx) {
        final AnalyticsFrameElement.RangeRows rangeRows =
                ctx.RANGE() != null ? AnalyticsFrameElement.RangeRows.RANGE : AnalyticsFrameElement.RangeRows.ROWS;

        elementStack.push(rangeRows);
    }

    @Override
    public void exitFrameStartOnly(QueryParser.FrameStartOnlyContext ctx) {
        final AnalyticsFrameElement frameElement = new AnalyticsFrameElement();
        final AnalyticsFrameElement.Frame frame = (AnalyticsFrameElement.Frame) elementStack.pop();
        frameElement.setFrameStart(frame);

        if (frame == AnalyticsFrameElement.Frame.PRECEDING || frame == AnalyticsFrameElement.Frame.FOLLOWING) {
            final NumberExpression numberExpression = (NumberExpression) elementStack.pop();
            frameElement.setFrameStartValue(numberExpression.getValue());
        }

        final AnalyticsFrameElement.RangeRows rangeRows = (AnalyticsFrameElement.RangeRows) elementStack.pop();
        frameElement.setRangeRows(rangeRows);
        elementStack.push(frameElement);
    }

    @Override
    public void exitFrameStartEnd(QueryParser.FrameStartEndContext ctx) {
        final AnalyticsFrameElement frameElement = new AnalyticsFrameElement();
        final AnalyticsFrameElement.Frame endFrame = (AnalyticsFrameElement.Frame) elementStack.pop();
        frameElement.setFrameEnd(endFrame);

        if (endFrame == AnalyticsFrameElement.Frame.PRECEDING || endFrame == AnalyticsFrameElement.Frame.FOLLOWING) {
            final NumberExpression numberExpression = (NumberExpression) elementStack.pop();
            frameElement.setFrameEndValue(numberExpression.getValue());
        }

        final AnalyticsFrameElement.Frame startFrame = (AnalyticsFrameElement.Frame) elementStack.pop();
        frameElement.setFrameStart(startFrame);

        if (startFrame == AnalyticsFrameElement.Frame.PRECEDING || startFrame == AnalyticsFrameElement.Frame.FOLLOWING) {
            final NumberExpression numberExpression = (NumberExpression) elementStack.pop();
            frameElement.setFrameStartValue(numberExpression.getValue());
        }

        final AnalyticsFrameElement.RangeRows rangeRows = (AnalyticsFrameElement.RangeRows) elementStack.pop();
        frameElement.setRangeRows(rangeRows);
        elementStack.push(frameElement);
    }

    @Override
    public void exitFrameStart(QueryParser.FrameStartContext ctx) {
        final TerminalNode number = ctx.NUMBER();
        if (number != null) {
            elementStack.push(new NumberExpression(number.getText()));
        }

        final AnalyticsFrameElement.Frame frameStart;
        if (ctx.UNBOUNDED() != null && ctx.PRECEDING() != null) {
            frameStart = AnalyticsFrameElement.Frame.UNBOUNDED_PRECEDING;
        } else if (ctx.PRECEDING() != null) {
            frameStart = AnalyticsFrameElement.Frame.PRECEDING;
        } else if (ctx.CURRENTROW() != null) {
            frameStart = AnalyticsFrameElement.Frame.CURRENT_ROW;
        } else {
            frameStart = AnalyticsFrameElement.Frame.FOLLOWING;
        }
        elementStack.push(frameStart);
    }

    @Override
    public void exitFrameEnd(QueryParser.FrameEndContext ctx) {
        final TerminalNode number = ctx.NUMBER();
        if (number != null) {
            elementStack.push(new NumberExpression(number.getText()));
        }

        final AnalyticsFrameElement.Frame frameEnd;
        if (ctx.UNBOUNDED() != null && ctx.FOLLOWING() != null) {
            frameEnd = AnalyticsFrameElement.Frame.UNBOUNDED_FOLLOWING;
        } else if (ctx.PRECEDING() != null) {
            frameEnd = AnalyticsFrameElement.Frame.PRECEDING;
        } else if (ctx.CURRENTROW() != null) {
            frameEnd = AnalyticsFrameElement.Frame.CURRENT_ROW;
        } else {
            frameEnd = AnalyticsFrameElement.Frame.FOLLOWING;
        }
        elementStack.push(frameEnd);
    }

    @Override
    public void exitQueryLayout(QueryParser.QueryLayoutContext ctx) {
        for (QueryElement queryElement : elementStack) {
            queryLayout.addExpressions((Expression) queryElement);
        }
    }

    @Override
    protected QueryLayout handleParsing() {
        parser.queryLayout();
        return queryLayout;
    }
}
