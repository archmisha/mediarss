package rss.rms.query.translator;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import rss.rms.query.expression.BetweenExpression;
import rss.rms.query.expression.RmsQueryExpression;

/**
 * Equals Expression Translator - translates to Mongo DB
 *
 * @author Mark Bramnik
 *         Date: 30/05/13
 *         Time: 11:08
 */
public class BetweenExpressionTranslator implements ExpressionTranslator<DBObject> {
    private static final String LEFT_BOUND_BTW_OP = "$gte";
    private static final String RIGHT_BOUND_BTW_OP = "$lte";
    private static final String LEFT_BOUND_NOT_BTW_OP = "$lt";
    private static final String RIGHT_BOUND_NOT_BTW_OP = "$gt";

    private static final String BTW_LOGICAL_OP = "$and";
    private static final String NOT_BTW_LOGICAL_OP = "$or";

    @Override
    public DBObject translateExpression(RmsQueryExpression exp, TranslationRulesManager<DBObject> translationRulesManager) {
        BetweenExpression btwExpression = (BetweenExpression) exp;
        if (btwExpression.hasNegation()) {
            return translateHelper(NOT_BTW_LOGICAL_OP, LEFT_BOUND_NOT_BTW_OP, RIGHT_BOUND_NOT_BTW_OP, btwExpression);
        } else {
            return translateHelper(BTW_LOGICAL_OP, LEFT_BOUND_BTW_OP, RIGHT_BOUND_BTW_OP, btwExpression);
        }
    }

    private DBObject translateHelper(String logicalOp, String leftBoundOp, String rightBoundOp, BetweenExpression exp) {
        BasicDBObject leftBoundValueObj = new BasicDBObject(leftBoundOp, ValueTranslatorHelper.translateValue(exp.getFieldPath(), exp.getLeftBoundValue()));
        BasicDBObject rightBoundValueObj = new BasicDBObject(rightBoundOp, ValueTranslatorHelper.translateValue(exp.getFieldPath(), exp.getRightBoundValue()));
        BasicDBObject leftBoundObj = new BasicDBObject(ValueTranslatorHelper.translateFieldName(exp.getFieldPath()), leftBoundValueObj);
        BasicDBObject rightBoundObj = new BasicDBObject(ValueTranslatorHelper.translateFieldName(exp.getFieldPath()), rightBoundValueObj);
        BasicDBList conditionList = new BasicDBList();
        conditionList.add(leftBoundObj);
        conditionList.add(rightBoundObj);
        return new BasicDBObject(logicalOp, conditionList);
    }
}
