package rss.rms.query.translator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import rss.rms.query.expression.EqExpression;
import rss.rms.query.expression.RmsQueryExpression;

/**
 * Equals Expression Translator - translates to Mongo DB
 *
 * @author Mark Bramnik
 */
public class EqExpressionTranslator implements ExpressionTranslator<DBObject> {

    @Override
    public DBObject translateExpression(RmsQueryExpression exp, TranslationRulesManager<DBObject> translationRulesManager) {
        // since there is no $eq operator in mongo db - we model it with {fieldPath : value} json
        EqExpression terminalExp = (EqExpression) exp;
        return new BasicDBObject(ValueTranslatorHelper.translateFieldName(terminalExp.getFieldPath()),
                ValueTranslatorHelper.translateValue(terminalExp.getFieldPath(), terminalExp.getValue()));
    }
}
