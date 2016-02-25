package rss.rms.query.parser;

import rss.commons.query.api.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Bramnik
 *         Date: 02/06/13
 *         Time: 10:06
 */
public class RmsQueryElementFactory {
    private static QueryElementFactory baseFactory = new QueryElementFactory();

    private interface ValueProcessor {
        Expression createExpression(Object value);
    }

    private static final class IntValueProcessor implements ValueProcessor {
        @Override
        public Expression createExpression(Object value) {
            return baseFactory.numberExpr((Integer) value);
        }
    }

    private static final class LongValueProcessor implements ValueProcessor {
        @Override
        public Expression createExpression(Object value) {
            return baseFactory.numberExpr((Long) value);
        }
    }

    private static final class StringValueProcessor implements ValueProcessor {
        @Override
        public Expression createExpression(Object value) {
            return baseFactory.stringExpr((String) value);
        }
    }

    private static final class BooleanValueProcessor implements ValueProcessor {
        @Override
        public Expression createExpression(Object value) {
            return new BooleanExpression((Boolean) value);
        }
    }

    private static final ValueProcessor INT_VALUE_PROCESSOR = new IntValueProcessor();
    private static final ValueProcessor LONG_VALUE_PROCESSOR = new LongValueProcessor();
    private static final ValueProcessor STRING_VALUE_PROCESSOR = new StringValueProcessor();
    private static final ValueProcessor BOOLEAN_VALUE_PROCESSOR = new BooleanValueProcessor();

    private static final Map<Class<?>, ValueProcessor> PROCESSORS = new HashMap<>();

    static {
        PROCESSORS.put(String.class, STRING_VALUE_PROCESSOR);
        PROCESSORS.put(Integer.class, INT_VALUE_PROCESSOR);
        PROCESSORS.put(Long.class, LONG_VALUE_PROCESSOR);
        PROCESSORS.put(Boolean.class, BOOLEAN_VALUE_PROCESSOR);
    }

    private ComparisonCondition createComparisonConditionInternal(String fieldPath, ComparisonCondition.Operator op, Object value, ValueProcessor valueProcessor) {
        PropertyExpression propertyFieldExpression = baseFactory.propertyExpr(fieldPath);
        Expression valueExpression = valueProcessor.createExpression(value);
        return baseFactory.comparisonCond(op, propertyFieldExpression, valueExpression);
    }

    private BetweenCondition betweenConditionInternal(String fieldPath, ValueProcessor valueProcessor, boolean hasNegation, Object leftBound, Object rightBound) {
        PropertyExpression propertyFieldExpression = baseFactory.propertyExpr(fieldPath);
        Expression leftBoundExpr = valueProcessor.createExpression(leftBound);
        Expression rightBoundExpr = valueProcessor.createExpression(rightBound);
        if (hasNegation) {
            return baseFactory.notBetweenCond(propertyFieldExpression, leftBoundExpr, rightBoundExpr);
        } else {
            return baseFactory.betweenCond(propertyFieldExpression, leftBoundExpr, rightBoundExpr);
        }
    }

    private InCondition inConditionInternal(String fieldPath, ValueProcessor valuesProcessor, boolean hasNegation, Object... values) {
        PropertyExpression propertyFieldExpression = baseFactory.propertyExpr(fieldPath);
        Expression[] valuesExpr = new Expression[values.length];
        for (int i = 0; i < values.length; i++) {
            valuesExpr[i] = valuesProcessor.createExpression(values[i]);
        }
        if (hasNegation) {
            return baseFactory.notInCondition(propertyFieldExpression, valuesExpr);
        } else {
            return baseFactory.inCond(propertyFieldExpression, valuesExpr);
        }
    }

    private ValueProcessor resolveProcessor(Object value) {
        return PROCESSORS.get(value.getClass());
    }

    public LogicalCondition and(Condition... conditions) {
        return baseFactory.logicalCond(LogicalCondition.Operator.AND, conditions);
    }

    public LogicalCondition or(Condition... conditions) {
        return baseFactory.logicalCond(LogicalCondition.Operator.OR, conditions);
    }

    public ExistsCondition existsCondition(String fieldPath, boolean isExists) {
        return new ExistsCondition(fieldPath, isExists);
    }


    public ComparisonCondition equalsCondition(String fieldPath, Object value) {
        return createComparisonConditionInternal(fieldPath, ComparisonCondition.Operator.EQUALS, value, resolveProcessor(value));
    }

    public ComparisonCondition notEqualsCondition(String fieldPath, Object value) {
        return createComparisonConditionInternal(fieldPath, ComparisonCondition.Operator.NOTEQUAL, value, resolveProcessor(value));
    }

    public ComparisonCondition lessCondition(String fieldPath, Object value) {
        return createComparisonConditionInternal(fieldPath, ComparisonCondition.Operator.LESS, value, resolveProcessor(value));
    }

    public ComparisonCondition lessOrEqualsCondition(String fieldPath, Object value) {
        return createComparisonConditionInternal(fieldPath, ComparisonCondition.Operator.LESSOREQUALS, value, resolveProcessor(value));
    }


    public ComparisonCondition greaterCondition(String fieldPath, Object value) {
        return createComparisonConditionInternal(fieldPath, ComparisonCondition.Operator.GREATER, value, resolveProcessor(value));
    }

    public ComparisonCondition greaterOrEqualsCondition(String fieldPath, Object value) {
        return createComparisonConditionInternal(fieldPath, ComparisonCondition.Operator.GREATEROREQUALS, value, resolveProcessor(value));
    }


    public BetweenCondition betweenCondition(String fieldPath, boolean hasNegation, Object leftBound, Object rightBound) {
        return betweenConditionInternal(fieldPath, resolveProcessor(leftBound), hasNegation, leftBound, rightBound);
    }

    public StartsWithCondition startsWithCondition(String fieldPath, String value) {
        PropertyExpression fieldPathExp = baseFactory.propertyExpr(fieldPath);
        StringExpression valueLiteralExp = baseFactory.stringExpr(value);
        return baseFactory.startsWithCondition(fieldPathExp, valueLiteralExp);
    }

    public InCondition inCondition(String fieldPath, boolean hasNegation, Object... values) {
        Object[] valuesArr = values;
        if (valuesArr.length == 0) {
            throw new IllegalArgumentException("Query Translation error: In condition on [" + fieldPath + "] can't be empty - at least one value must be supplied");
        }
        return inConditionInternal(fieldPath, resolveProcessor(values[0]), hasNegation, values);
    }

    public QueryLayout layout(PropertyExpression... propertyExpressions) {
        return baseFactory.layout(propertyExpressions);
    }

    public QueryOrder order(OrderExpression... orderExpressions) {
        return baseFactory.order(orderExpressions);
    }

    public OrderExpression orderExpression(Expression expr, OrderExpression.Direction dir) {
        return baseFactory.orderExpr(expr, dir);
    }

    public PropertyExpression propertyExpression(String path) {
        return baseFactory.propertyExpr(path);
    }

    public QueryFilter filter(Condition condition) {
        return baseFactory.filter(condition);
    }

    public QueryMeta meta(PropertyExpression... propertyExpressions) {
        return baseFactory.meta(propertyExpressions);
    }

    public WrappedCondition wrappedCond(Condition condition) {
        return baseFactory.wrappedCond(condition);
    }


}
