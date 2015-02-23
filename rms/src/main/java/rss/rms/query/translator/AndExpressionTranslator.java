package rss.rms.query.translator;

import com.mongodb.DBObject;
import rss.rms.query.expression.LogicalExpression;
import rss.rms.query.expression.RmsQueryExpression;

/**
 * And Expression Translator - translates to Mongo DB
 *
 * @author Mark Bramnik
 */
public class AndExpressionTranslator implements ExpressionTranslator<DBObject> {
    @Override
    public DBObject translateExpression(RmsQueryExpression exp, TranslationRulesManager<DBObject> translationRulesManager) {
        return ExpressionTranslatorHelper.translateLogicalExpression((LogicalExpression) exp, "$and", translationRulesManager);
    }
}
