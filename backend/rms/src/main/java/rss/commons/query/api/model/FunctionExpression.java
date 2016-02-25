package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static rss.commons.query.api.model.SyntaxConst.*;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class FunctionExpression implements Expression {

    private String name;
    private final List<Expression> parameters = new ArrayList<>();
    private AnalyticsFunctionExpression analyticsFunctionExpression;

    /**
     * @return the function name.
     */
    public final String getName() {
        return name;
    }

    /**
     * @param name the function name to set.
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * @return a {@link java.util.List} of function arguments.
     */
    public final List<Expression> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters for this function expression
     *
     * @param parameters an Expression list.
     */
    public final void addParameters(List<Expression> parameters) {
        this.parameters.addAll(parameters);
    }

    /**
     * Sets parameters for this function expression
     *
     * @param parameters an Expression list.
     */
    public final void addParameters(Expression... parameters) {
        Collections.addAll(this.parameters, parameters);
    }

    public AnalyticsFunctionExpression getAnalyticsFunctionExpression() {
        return analyticsFunctionExpression;
    }

    public void setAnalyticsFunctionExpression(AnalyticsFunctionExpression analyticsFunctionExpression) {
        this.analyticsFunctionExpression = analyticsFunctionExpression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (name != null) {
            stringBuilder.append(name);
        }
        stringBuilder.append(LPAREN);
        ApiStringUtils.appendApiCommaSeparatedList(parameters, stringBuilder);
        stringBuilder.append(RPAREN);
        if (analyticsFunctionExpression != null) {
            stringBuilder.append(SPACE);
            stringBuilder.append(analyticsFunctionExpression);
        }
        return stringBuilder.toString();
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
        for (Expression parameter : parameters) {
            parameter.acceptVisitor(visitor, context);
        }
        if (analyticsFunctionExpression != null) {
            analyticsFunctionExpression.acceptVisitor(visitor, context);
        }

        visitor.exit(this, context);
    }
}
