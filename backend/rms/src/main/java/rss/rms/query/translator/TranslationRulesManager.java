package rss.rms.query.translator;

import rss.rms.query.expression.RmsQueryExpression;

/**
 * A registry for all available translation rules -
 * A helper class for query translation to mongo representation
 *
 * @author Mark Bramnik
 *         Date: 2/28/13
 *         Time: 1:50 PM
 * @since 1.0.0-9999
 */
public interface TranslationRulesManager<T> {
    /**
     * Obtain a translator for the type of expression
     * The translator should be registered in advance
     *
     * @param expression the class of expression to be translated
     * @return the assigned translator
     */
    ExpressionTranslator<T> getTranslator(Class<? extends RmsQueryExpression> expression);

}
