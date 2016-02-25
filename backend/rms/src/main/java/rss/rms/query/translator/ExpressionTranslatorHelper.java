package rss.rms.query.translator;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import rss.rms.query.expression.LogicalExpression;
import rss.rms.query.expression.RmsQueryExpression;
import rss.rms.query.expression.SimpleComparisonTerminalExpression;

import java.util.List;

/**
 * Expression Translator Helper - an auxiliary tool for translation to Mongo DB
 *
 * @author Mark Bramnik
 *         Date: 30/05/13
 *         Time: 12:00
 */
class ExpressionTranslatorHelper {
    public static DBObject translateSimpleComparisonTerminalExpression(SimpleComparisonTerminalExpression terminalExp, String mongoDbOpName) {
        BasicDBObject valueObject = new BasicDBObject(mongoDbOpName, ValueTranslatorHelper.translateValue(terminalExp.getFieldPath(), terminalExp.getValue()));
        return new BasicDBObject(ValueTranslatorHelper.translateFieldName(terminalExp.getFieldPath()), valueObject);
    }

    public static DBObject translateLogicalExpression(LogicalExpression logicalExp, String mongoDbOpName, TranslationRulesManager<DBObject> translationRulesManager) {

        BasicDBList conditionList = new BasicDBList();
        List<RmsQueryExpression> subExpressions = logicalExp.getSubExpressions();
        for (RmsQueryExpression subExpression : subExpressions) {
            ExpressionTranslator<DBObject> translator = translationRulesManager.getTranslator(subExpression.getClass());
            DBObject translatedSubExpression = translator.translateExpression(subExpression, translationRulesManager);
            conditionList.add(translatedSubExpression);
        }
        return new BasicDBObject(mongoDbOpName, conditionList);

    }
}
