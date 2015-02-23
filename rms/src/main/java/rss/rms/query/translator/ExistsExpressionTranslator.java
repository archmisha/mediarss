package rss.rms.query.translator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import rss.rms.query.expression.ExistsExpression;
import rss.rms.query.expression.RmsQueryExpression;

/**
 * Exists Expression Translator - translates to Mongo DB
 *
 * @author Mark Bramnik
 *         Date: 30/05/13
 *         Time: 11:08
 */
public class ExistsExpressionTranslator implements ExpressionTranslator<DBObject> {

    @Override
    public DBObject translateExpression(RmsQueryExpression exp, TranslationRulesManager<DBObject> translationRulesManager) {
        ExistsExpression existsExpression = (ExistsExpression) exp;
        BasicDBObject valueObject = new BasicDBObject("$exists", isExists(existsExpression));
        return new BasicDBObject(ValueTranslatorHelper.translateFieldName(existsExpression.getFieldPath()),
                ValueTranslatorHelper.translateValue(existsExpression.getFieldPath(), valueObject));
    }

    private boolean isExists(ExistsExpression existsExpression) {
        return !existsExpression.hasNegation();
    }
}
