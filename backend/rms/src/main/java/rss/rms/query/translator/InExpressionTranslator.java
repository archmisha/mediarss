package rss.rms.query.translator;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import rss.rms.query.expression.InExpression;
import rss.rms.query.expression.RmsQueryExpression;

/**
 * In Expression Translator - translates to Mongo DB
 *
 * @author Mark Bramnik
 *         Date: 30/05/13
 *         Time: 11:10
 *         To change this template use File | Settings | File Templates.
 */
public class InExpressionTranslator implements ExpressionTranslator<DBObject> {
    private static final String IN_OPERATOR_NAME = "$in";
    private static final String NOT_IN_OPERATOR_NAME = "$nin";

    @Override
    public DBObject translateExpression(RmsQueryExpression exp, TranslationRulesManager<DBObject> translationRulesManager) {
        InExpression inExpression = (InExpression) exp;
        BasicDBList valuesList = new BasicDBList();
        for (Object value : inExpression.getValues()) {
            valuesList.add(ValueTranslatorHelper.translateValue(inExpression.getFieldPath(), value));
        }
        String op = getOperatorName(inExpression);
        BasicDBObject valueObject = new BasicDBObject(op, valuesList);
        return new BasicDBObject(ValueTranslatorHelper.translateFieldName(inExpression.getFieldPath()), valueObject);
    }

    private String getOperatorName(InExpression inExpression) {
        if (!inExpression.hasNegation()) {
            return IN_OPERATOR_NAME;
        } else {
            return NOT_IN_OPERATOR_NAME;
        }
    }
}
