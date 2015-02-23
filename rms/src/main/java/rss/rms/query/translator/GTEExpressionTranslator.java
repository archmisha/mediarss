package rss.rms.query.translator;

import com.mongodb.DBObject;
import rss.rms.query.expression.RmsQueryExpression;
import rss.rms.query.expression.SimpleComparisonTerminalExpression;

/**
 * Greater or equal Expression Translator - translates to Mongo DB
 *
 * @author Mark Bramnik
 *         Date: 30/05/13
 *         Time: 11:09
 *         To change this template use File | Settings | File Templates.
 */
public class GTEExpressionTranslator implements ExpressionTranslator<DBObject> {

    @Override
    public DBObject translateExpression(RmsQueryExpression exp, TranslationRulesManager<DBObject> translationRulesManager) {
        return ExpressionTranslatorHelper.translateSimpleComparisonTerminalExpression((SimpleComparisonTerminalExpression) exp, "$gte");
    }
}
