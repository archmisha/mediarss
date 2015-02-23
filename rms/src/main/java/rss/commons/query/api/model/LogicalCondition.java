package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class LogicalCondition implements Condition {

    /**
     * Logical operators enumeration.
     */
    public enum Operator implements QueryElement {
        /***/
        AND(SyntaxConst.KW_AND),
        /***/
        OR(SyntaxConst.KW_OR);

        private final String apiString;

        private Operator(String apiString) {
            this.apiString = apiString;
        }

        /**
         * @return the QueryImpl API string representation of this operator.
         */
        @Override
        public String toApiString() {
            return apiString;
        }
    }

    private Operator operator;
    private final List<Condition> conditions = new ArrayList<>();

    /**
     * @return the {@link com.hp.maas.platform.commons.query.api.model.LogicalCondition.Operator} for this condition.
     */
    public final Operator getOperator() {
        return operator;
    }

    /**
     * @param operator an {@link com.hp.maas.platform.commons.query.api.model.LogicalCondition.Operator} to set.
     */
    public final void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * @return the {@link java.util.List} of set {@link com.hp.maas.platform.commons.query.api.model.Condition}s.
     */
    public final List<Condition> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    /**
     * @param conditions a {@link java.util.List} of {@link com.hp.maas.platform.commons.query.api.model.Condition}s to set.
     */
    public final void addConditions(List<Condition> conditions) {
        this.conditions.addAll(conditions);
    }

    /**
     * @param conditions a {@link java.util.List} of {@link com.hp.maas.platform.commons.query.api.model.Condition}s to set.
     */
    public final void addConditions(Condition... conditions) {
        Collections.addAll(this.conditions, conditions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!conditions.isEmpty()) {
            for (int i = 0; i < conditions.size(); i++) {
                if (i != 0) {
                    stringBuilder.append(SyntaxConst.SPACE);
                    if (operator != null) {
                        stringBuilder.append(operator.toApiString());
                        stringBuilder.append(SyntaxConst.SPACE);
                    }
                }

                Condition condition = conditions.get(i);
                stringBuilder.append(condition);
            }
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
    public final <T> void acceptVisitor(ConditionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
        for (Condition condition : conditions) {
            condition.acceptVisitor(visitor, context);
        }
        visitor.exit(this, context);
    }
}
