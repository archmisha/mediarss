package rss.rms.query.translator;

import com.mongodb.DBObject;
import rss.rms.query.expression.RmsQueryExpression;
import rss.rms.query.expression.SimpleComparisonTerminalExpression;

/**
 * Not Equals Expression Translator - translates to Mongo DB
 *
 * @author Mark Bramnik
 *         Date: 30/05/13
 *         Time: 11:10
 */
public class NotEqExpressionTranslator implements ExpressionTranslator<DBObject> {

    @Override
    public DBObject translateExpression(RmsQueryExpression exp, TranslationRulesManager<DBObject> translationRulesManager) {
        return ExpressionTranslatorHelper.translateSimpleComparisonTerminalExpression((SimpleComparisonTerminalExpression) exp, "$ne");
    }
}
