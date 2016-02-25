package rss.rms.query.translator;

import rss.rms.query.expression.RmsQueryExpression;

/**
 * A contract for translator of expression of one specific type
 *
 * @author Mark Bramnik
 *         Date: 30/05/13
 *         Time: 11:10
 */
public interface ExpressionTranslator<T> {

    T translateExpression(RmsQueryExpression exp, TranslationRulesManager<T> translationRulesManager);
}
