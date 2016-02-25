package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class provides a convenient API for constructing query model objects.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/7/13
 */
public class QueryElementFactory {

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.NumberExpression}
     *
     * @param number a number expression as string.
     * @return a NumberExpression
     */
    public NumberExpression numberExpr(String number) {
        return new NumberExpression(number);
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.NumberExpression}
     *
     * @param number a number expression as long.
     * @return a NumberExpression
     */
    public NumberExpression numberExpr(long number) {
        return new NumberExpression(String.valueOf(number));
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.NumberExpression}
     *
     * @param number a number expression as double.
     * @return a NumberExpression
     */
    public NumberExpression numberExpr(double number) {
        return new NumberExpression(String.valueOf(number));
    }

    /**
     * Creates a new instance of {@link StringExpression}
     *
     * @param literal a string
     * @return a StringExpression
     */
    public StringExpression stringExpr(String literal) {
        StringExpression stringExpression = new StringExpression();
        stringExpression.setValue(literal);
        return stringExpression;
    }

    /**
     * Creates a new instance of {@link BooleanExpression}
     *
     * @param value a boolean value
     * @return a BooleanExpression
     */
    public BooleanExpression booleanExpr(boolean value) {
        return new BooleanExpression(value);
    }

    /**
     * Creates a new instance of {@link BooleanExpression}
     *
     * @param value a boolean value
     * @return a BooleanExpression
     */
    public BooleanExpression booleanExpr(String value) {
        return new BooleanExpression(value);
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.BinaryExpression}
     *
     * @param op        the binary Operator to set
     * @param leftExpr  the left side Expression
     * @param rightExpr the right side Expression
     * @return a BinaryExpression
     */
    public BinaryExpression binaryExpr(BinaryExpression.Operator op, Expression leftExpr, Expression rightExpr) {
        BinaryExpression binaryExpression = new BinaryExpression();
        binaryExpression.setLeftExpression(leftExpr);
        binaryExpression.setRightExpression(rightExpr);
        binaryExpression.setOperator(op);
        return binaryExpression;
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.BinaryExpression} with a chain
     * (possible more than two) expressions.
     * <p>
     * Use this method to create expressions of the form: x + y + z + ...
     * </p>
     *
     * @param op          the binary Operator to set
     * @param firstExpr   the first Expression in the chain
     * @param secondExpr  the right side Expression
     * @param expressions the expressions to set
     * @return a BinaryExpression
     */
    public final BinaryExpression binaryExpr(BinaryExpression.Operator op, Expression firstExpr, Expression secondExpr, Expression... expressions) {


        List<Expression> expressionList = new ArrayList<>(expressions.length + 2);
        expressionList.add(firstExpr);
        expressionList.add(secondExpr);
        Collections.addAll(expressionList, expressions);

        BinaryExpression binaryExpression = null;

        /*
         Process the list in reverse order: from the right-most expression to the left-most.

         The list index starts from the right-most expression and ends at value 1.
         The reason for ending at value 1 is that each iteration in the loop removes two expressions from the end of the
         list; the last expression which is either the right expression (in case the the "expressions" var-args
         parameter is empty), or a BinaryExpression instance created by a previous iteration of the loop; and the second
         which is the left expression of the binary expression created in this iteration.
         Each iteration adds the BinaryExpression it creates to the end of the list, for the following iteration to
         handle, this means that last iteration should leave the root BinaryExpression in the list and the loop should
         end.

         */
        for (int i = (expressionList.size() - 1); i > 0; ) {
            binaryExpression = new BinaryExpression();

            binaryExpression.setOperator(op);
            binaryExpression.setRightExpression(expressionList.remove(i--));
            binaryExpression.setLeftExpression(expressionList.remove(i));

            // Add the current expression to the end of the list for further handling.
            expressionList.add(binaryExpression);
        }

        return binaryExpression;
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.FunctionExpression}
     *
     * @param name       the function name
     * @param parameters optional function parameter Expressions
     * @return a FunctionExpression
     */
    public FunctionExpression functionExpr(String name, Expression... parameters) {
        FunctionExpression functionExpression = new FunctionExpression();
        functionExpression.setName(name);
        functionExpression.addParameters(parameters);

        return functionExpression;
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.PropertyExpression}
     *
     * @param parts property parts. Will be translated to "part1.part2.part3" etc.
     * @return a PropertyExpression
     */
    public PropertyExpression propertyExpr(String... parts) {
        PropertyExpression propertyExpression = new PropertyExpression();
        propertyExpression.addSegments(parts);
        return propertyExpression;
    }

    /**
     * Creates a new instance of {@link OrderExpression )
     *
     * @param expr the order Expression to set.
     * @return an OrderExpression
     */
    public OrderExpression orderExpr(Expression expr) {
        return orderExpr(expr, OrderExpression.Direction.ASC);
    }

    /**
     * Creates a new instance of {@link OrderExpression}
     *
     * @param expr the order Expression to set.
     * @param dir  the order Direction to set.
     * @return an OrderExpression
     */
    public OrderExpression orderExpr(Expression expr, OrderExpression.Direction dir) {
        OrderExpression orderExpression = new OrderExpressionImpl();
        orderExpression.setExpression(expr);
        orderExpression.setDirection(dir);

        return orderExpression;
    }

    /**
     * Creates a new instance of {@link UnaryExpression}
     *
     * @param op   the Operator to set.
     * @param expr the Expression to set.
     * @return an UnaryExpression
     */
    public UnaryExpression unaryExpr(UnaryExpression.Operator op, Expression expr) {
        UnaryExpression unaryExpression = new UnaryExpression();
        unaryExpression.setExpression(expr);
        unaryExpression.setOperator(op);

        return unaryExpression;
    }

    /**
     * Creates a new instance of {@link WrappedExpression}
     *
     * @param expr the Expression to set.
     * @return an WrappedExpression
     */
    public WrappedExpression wrappedExpr(Expression expr) {
        WrappedExpression wrappedExpression = new WrappedExpression();
        wrappedExpression.setExpression(expr);

        return wrappedExpression;
    }

    /**
     * Creates a new instance of {@link VariableExpression}
     *
     * @param varName the variable name.
     * @return a VariableExpression
     */
    public VariableExpression varibaleExpr(String varName) {
        return new VariableExpression(varName);
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.BetweenCondition}
     *
     * @param expr      the Expression to evaluate.
     * @param leftExpr  the left boundary Expression to evaluate against.
     * @param rightExpr the right boundary Expression to evaluate against.
     * @return a BetweenCondition
     */
    public BetweenCondition betweenCond(Expression expr, Expression leftExpr, Expression rightExpr) {
        BetweenCondition betweenCondition = new BetweenCondition();
        betweenCondition.setExpression(expr);
        betweenCondition.setLeftBoundary(leftExpr);
        betweenCondition.setRightBoundary(rightExpr);

        return betweenCondition;
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.BetweenCondition} set to evaluate negative condition.
     *
     * @param expr      the Expression to evaluate.
     * @param leftExpr  the left boundary Expression to evaluate against.
     * @param rightExpr the right boundary Expression to evaluate against.
     * @return a BetweenCondition
     */
    public BetweenCondition notBetweenCond(Expression expr, Expression leftExpr, Expression rightExpr) {
        BetweenCondition betweenCondition = betweenCond(expr, leftExpr, rightExpr);
        betweenCondition.setNot(true);

        return betweenCondition;
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.ComparisonCondition}
     *
     * @param op        the Operator to set.
     * @param leftExpr  the left Expression.
     * @param rightExpr the right Expression.
     * @return a ComparisonCondition
     */
    public ComparisonCondition comparisonCond(ComparisonCondition.Operator op, Expression leftExpr, Expression rightExpr) {
        ComparisonCondition comparisonCondition = new ComparisonCondition();
        comparisonCondition.setLeftExpression(leftExpr);
        comparisonCondition.setRightExpression(rightExpr);
        comparisonCondition.setOperator(op);

        return comparisonCondition;
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.InCondition}
     *
     * @param expr        the Expression to evaluate.
     * @param expressions the Expressions to evaluate against.
     * @return an InCondition
     */
    public InCondition inCond(Expression expr, Expression... expressions) {
        InCondition inCondition = new InCondition();
        inCondition.setExpression(expr);
        inCondition.addExpressions(expressions);

        return inCondition;
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.InCondition} set to evaluate a negative condition.
     *
     * @param expr        the Expression to evaluate.
     * @param expressions the Expressions to evaluate against.
     * @return an InCondition
     */
    public InCondition notInCondition(Expression expr, Expression... expressions) {
        InCondition inCondition = inCond(expr, expressions);
        inCondition.setNot(true);

        return inCondition;
    }

    /**
     * Creates a new instance of {@link com.hp.maas.platform.commons.query.api.model.LogicalCondition}
     *
     * @param op         the Operator to set.
     * @param conditions the Conditions to set.
     * @return a LogicalCondition
     */
    public LogicalCondition logicalCond(LogicalCondition.Operator op, Condition... conditions) {
        LogicalCondition logicalCondition = new LogicalCondition();
        logicalCondition.setOperator(op);
        logicalCondition.addConditions(conditions);

        return logicalCondition;
    }

    /**
     * Creates a new instance of {@link WrappedCondition}
     *
     * @param condition the Condition to wrap with parenthesis.
     * @return a WrappedCondition
     */
    public WrappedCondition wrappedCond(Condition condition) {
        WrappedCondition wrappedCondition = new WrappedCondition();
        wrappedCondition.setCondition(condition);

        return wrappedCondition;
    }

    /**
     * Creates a new instance of {@link NullCondition}
     *
     * @param expression the expression to compare to null.
     * @return a NullCondition
     */
    public NullCondition nullCond(Expression expression) {
        return nullCond(expression, false);
    }

    /**
     * Creates a new instance of {@link NullCondition}
     *
     * @param expression the expression to compare to null.
     * @param not        specifies whether to create a negative comparison condition.
     * @return a NullCondition
     */
    public NullCondition nullCond(Expression expression, boolean not) {
        NullCondition nullCondition = new NullCondition();
        nullCondition.setExpression(expression);
        nullCondition.setNot(not);

        return nullCondition;
    }

    /**
     * Creates a new instance of {@link VectorCondition}
     *
     * @param vectorName the name of the vector to set condition on.
     * @param condition  the condition on the specified vector name.
     * @return a VectorCondition
     */
    public VectorCondition vectorCondition(String vectorName, Condition condition) {
        VectorCondition vectorCondition = new VectorCondition();

        vectorCondition.setVectorName(vectorName);
        vectorCondition.setCondition(condition);

        return vectorCondition;
    }

    /**
     * Creates a new instance of {@link VectorCondition} with 'notexists' semantics
     *
     * @param vectorName the name of the vector to set condition on.
     * @param condition  the condition on the specified vector name.
     * @return a VectorCondition
     */
    public VectorCondition notExistsVectorCondition(String vectorName, Condition condition) {
        VectorCondition vectorCondition = vectorCondition(vectorName, condition);
        vectorCondition.setNotexists(true);

        return vectorCondition;
    }

    /**
     * Starts with condition.
     *
     * @param expression the expression
     * @param pattern    the pattern
     * @return the starts with condition
     */
    public StartsWithCondition startsWithCondition(Expression expression, StringExpression pattern) {
        StartsWithCondition startsWithCondition = new StartsWithCondition();
        startsWithCondition.setExpression(expression);
        startsWithCondition.setPattern(pattern);
        return startsWithCondition;
    }

    /**
     * Creates a new instance of {@link QueryLayout}
     *
     * @param expressions the Expressions to set.
     * @return a QueryLayout
     */
    public QueryLayout layout(Expression... expressions) {
        QueryLayout queryLayout = new QueryLayoutImpl();
        queryLayout.addExpressions(expressions);
        return queryLayout;
    }


    /**
     * Creates a new instance of {@link QueryGroup}
     *
     * @param expressions the Expressions to set.
     * @return a QueryGroupBy
     */
    public QueryGroup groupBy(Expression... expressions) {
        QueryGroup queryGroupBy = new QueryGroupImpl();
        queryGroupBy.addExpressions(expressions);
        return queryGroupBy;
    }

    /**
     * Creates a new instance of {@link QueryFilter}
     *
     * @param cond the filter Condition to set.
     * @return a QueryFilter
     */
    public QueryFilter filter(Condition cond) {
        QueryFilter queryFilter = new QueryFilterImpl();
        queryFilter.setCondition(cond);
        return queryFilter;
    }

    /**
     * Creates a new instance of {@link QueryOrder}
     *
     * @param expressions the OrderExpressions to set.
     * @return a QueryOrder
     */
    public QueryOrder order(OrderExpression... expressions) {
        QueryOrder queryOrder = new QueryOrderImpl();
        queryOrder.addExpressions(expressions);

        return queryOrder;
    }

    /**
     * Creates a new instance of {@link QueryPage}
     *
     * @param offset the page start offset to set.
     * @param size   the page size to set.
     * @return a QueryPage
     */
    @Deprecated
    public QueryPage page(int offset, int size) {
        QueryPage queryPage = new QueryPageImpl();
        queryPage.setOffset(offset);
        queryPage.setSize(size);

        return queryPage;
    }

    /**
     * Creates a new instance of {@link QueryMeta}
     *
     * @param propertyExpressions one or more
     * @return a QueryMeta
     */
    public QueryMeta meta(PropertyExpression... propertyExpressions) {
        QueryMeta meta = new QueryMetaImpl();
        meta.addPropertyExpressions(propertyExpressions);

        return meta;
    }

    /**
     * Creates a new instance of empty {@link Query}
     *
     * @return a Query
     */
    public Query query() {
        return new QueryImpl();
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @param layout the QueryLayout to set.
     * @param filter the QueryFilter to set.
     * @return a Query
     */
    public Query query(QueryLayout layout, QueryFilter filter) {
        return query(layout, filter, null, -1, -1);
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @param layout the QueryLayout to set.
     * @param filter the QueryFilter to set.
     * @param page   the QueryPage to set.
     * @return a Query
     * @deprecated use {@link #query(QueryLayout, QueryFilter, int, int)} instead
     */
    @Deprecated
    public Query query(QueryLayout layout, QueryFilter filter, QueryPage page) {
        return query(layout, filter, null, page);
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @param layout the QueryLayout to set.
     * @param filter the QueryFilter to set.
     * @param order  the QueryOrder to set.
     * @param page   the QueryPage to set.
     * @return a Query
     * @deprecated use {@link #query(QueryLayout, QueryFilter, QueryOrder, int, int)} instead
     */
    @Deprecated
    public Query query(QueryLayout layout, QueryFilter filter, QueryOrder order, QueryPage page) {
        QueryImpl query = new QueryImpl();
        query.setLayout(layout);
        query.setFilter(filter);
        query.setOrder(order);
        query.setPage(page);

        return query;
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @param layout the QueryLayout to set.
     * @param filter the QueryFilter to set.
     * @param group  the QueryGroup to set.
     * @param order  the QueryOrder to set.
     * @param page   the QueryPage to set.
     * @param meta   the QueryMeta to set.
     * @return a Query
     * @deprecated use {@link #query(QueryLayout, QueryFilter, QueryGroup, QueryOrder, QueryMeta, int, int)} instead
     */
    @Deprecated
    public Query query(QueryLayout layout, QueryFilter filter, QueryGroup group, QueryOrder order, QueryPage page, QueryMeta meta) {
        QueryImpl query = new QueryImpl();
        query.setLayout(layout);
        query.setFilter(filter);
        query.setGroup(group);
        query.setOrder(order);
        query.setPage(page);
        query.setMeta(meta);

        return query;
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @param layout the QueryLayout to set.
     * @param filter the QueryFilter to set.
     * @param skip   the number of results to skip
     * @param size   the number of results to return.
     * @return a Query
     */
    public Query query(QueryLayout layout, QueryFilter filter, int skip, int size) {
        return query(layout, filter, null, skip, size);
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @param layout the QueryLayout to set.
     * @param filter the QueryFilter to set.
     * @param order  the QueryOrder to set.
     * @param skip   the number of results to skip
     * @param size   the number of results to return.
     * @return a Query
     */
    public Query query(QueryLayout layout, QueryFilter filter, QueryOrder order, int skip, int size) {
        return query(layout, filter, null, order, null, skip, size);
    }

    /**
     * Creates a new instance of {@link Query}
     *
     * @param layout the QueryLayout to set.
     * @param filter the QueryFilter to set.
     * @param group  the QueryGroup to set.
     * @param order  the QueryOrder to set.
     * @param meta   the QueryMeta to set.
     * @param skip   the number of results to skip
     * @param size   the number of results to return.
     * @return a Query
     */
    public Query query(QueryLayout layout, QueryFilter filter, QueryGroup group, QueryOrder order, QueryMeta meta, int skip, int size) {
        QueryImpl query = new QueryImpl();
        query.setLayout(layout);
        query.setFilter(filter);
        query.setGroup(group);
        query.setOrder(order);
        query.setMeta(meta);
        query.setSkip(skip);
        query.setSize(size);

        return query;
    }

    /**
     * Returns a new empty QueryLayout instance.
     *
     * @return a QueryLayout
     */
    public QueryLayout emptyLayout() {
        return new QueryLayoutImpl();
    }

    /**
     * Returns a new empty QueryGroupBy instance.
     *
     * @return a QueryLayout
     */
    public QueryGroup emptyGroupBy() {
        return new QueryGroupImpl();
    }

    /**
     * Returns a new empty QueryFilter instance.
     *
     * @return a QueryFilter
     */
    public QueryFilter emptyFilter() {
        return new QueryFilterImpl();
    }

    /**
     * Returns a new empty QueryOrder instance.
     *
     * @return a QueryOrder
     */
    public QueryOrder emptyOrder() {
        return new QueryOrderImpl();
    }

    /**
     * Returns a new empty QueryPage instance.
     *
     * @return a QueryPage
     * @deprecated {@link QueryPage} is deprecated. Use {@link Query#setSkip(int)} and {@link Query#setSize(int)} instead.
     */
    @Deprecated
    public QueryPage emptyPage() {
        return new QueryPageImpl();
    }

    /**
     * Returns a new empty QueryMeta instance.
     *
     * @return a QueryMeta
     */
    public QueryMeta emptyMeta() {
        return new QueryMetaImpl();
    }
}
