package rss.rms.query.translator;

import com.mongodb.DBObject;
import rss.rms.query.expression.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the registry of translators to Mongo db - should be used by the translation algorithm
 *
 * @author Mark Bramnik
 *         Date: 02/06/13
 *         Time: 13:17
 */
public class MongoTranslationRulesCreator implements TranslationRulesCreator<DBObject> {
    @Override
    public Map<Class<? extends RmsQueryExpression>, ExpressionTranslator<DBObject>> createTranslationRegistry() {
        Map<Class<? extends RmsQueryExpression>, ExpressionTranslator<DBObject>> translatorRegistry = new HashMap<>();
        translatorRegistry.put(AndExpression.class, new AndExpressionTranslator());
        translatorRegistry.put(OrExpression.class, new OrExpressionTranslator());
        translatorRegistry.put(EqExpression.class, new EqExpressionTranslator());
        translatorRegistry.put(LTExpression.class, new LTExpressionTranslator());
        translatorRegistry.put(LTEExpression.class, new LTEExpressionTranslator());
        translatorRegistry.put(GTExpression.class, new GTExpressionTranslator());
        translatorRegistry.put(GTEExpression.class, new GTEExpressionTranslator());
        translatorRegistry.put(NotEqExpression.class, new NotEqExpressionTranslator());
        translatorRegistry.put(BetweenExpression.class, new BetweenExpressionTranslator());
        translatorRegistry.put(ExistsExpression.class, new ExistsExpressionTranslator());
        translatorRegistry.put(InExpression.class, new InExpressionTranslator());
        translatorRegistry.put(StartsWithExpression.class, new StartsWithExpressionTranslator());
        return translatorRegistry;
    }
}
