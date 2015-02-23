package rss.rms.query.translator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import rss.rms.query.expression.RmsQueryExpression;
import rss.rms.query.expression.StartsWithExpression;

/**
 * User: Mark Bramnik
 * Date: 09/09/13
 * Time: 12:40
 */
public class StartsWithExpressionTranslator implements ExpressionTranslator<DBObject> {

    private static final String LINE_START = "^";

    @Override
    public DBObject translateExpression(RmsQueryExpression exp, TranslationRulesManager<DBObject> translationRulesManager) {
        StartsWithExpression startsWithExpression = (StartsWithExpression) exp;

        BasicDBObject valueObject = new BasicDBObject("$regex", ValueTranslatorHelper.translateValue(startsWithExpression.getFieldPath(), LINE_START + startsWithExpression.getValue()));
        return new BasicDBObject(ValueTranslatorHelper.translateFieldName(startsWithExpression.getFieldPath()),
                ValueTranslatorHelper.translateValue(startsWithExpression.getFieldPath(), valueObject));
    }
}
