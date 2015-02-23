package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sbruce
 * Date: 02/09/2013
 * Time: 10:59
 */
public class AnalyticsFunctionExpression implements Expression {

    private Expression partitionExpression;
    private final List<OrderExpression> orderExpressions = new ArrayList<>();
    private AnalyticsFrameElement frameElement;

    public Expression getPartitionExpression() {
        return partitionExpression;
    }

    public void setPartitionExpression(Expression partitionExpression) {
        this.partitionExpression = partitionExpression;
    }

    /**
     * @return the list of {@link OrderExpression}s assigned.
     */
    public final List<OrderExpression> getOrderExpressions() {
        return orderExpressions;
    }

    /**
     * @param orderExpressions one or more {@link OrderExpression}s to add.
     */
    public final void addOrderExpressions(OrderExpression... orderExpressions) {
        Collections.addAll(this.orderExpressions, orderExpressions);
    }

    public AnalyticsFrameElement getFrameElement() {
        return frameElement;
    }

    public void setFrameElement(AnalyticsFrameElement frameElement) {
        this.frameElement = frameElement;
    }

    @Override
    public <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context) {
        if (visitor instanceof AnalyticsExpressionVisitor) {
            ((AnalyticsExpressionVisitor) visitor).visit(this, context);
        }
    }

    @Override
    public String toString() {
        return toApiString();
    }

    @Override
    public String toApiString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SyntaxConst.KW_OVER);
        stringBuilder.append(SyntaxConst.LPAREN);
        if (partitionExpression != null) {
            stringBuilder.append("partition");
            stringBuilder.append(SyntaxConst.LPAREN);
            stringBuilder.append(partitionExpression);
            stringBuilder.append(SyntaxConst.RPAREN);
        }
        if (!orderExpressions.isEmpty()) {
            stringBuilder.append("order by");
            stringBuilder.append(SyntaxConst.LPAREN);
            ApiStringUtils.appendApiCommaSeparatedList(orderExpressions, stringBuilder);
            stringBuilder.append(SyntaxConst.RPAREN);
        }
        if (frameElement != null) {
            stringBuilder.append("frame");
            stringBuilder.append(SyntaxConst.LPAREN);
            stringBuilder.append(frameElement);
            stringBuilder.append(SyntaxConst.RPAREN);
        }
        stringBuilder.append(SyntaxConst.RPAREN);
        return stringBuilder.toString();
    }
}
