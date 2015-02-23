package rss.rms.query.translator;

import rss.rms.query.expression.RmsQueryExpression;

import java.util.Map;

/**
 * An interface that defines a contract for rules translation registry creation
 *
 * @author Mark Bramnik
 *         Date: 02/06/13
 *         Time: 13:16
 */
public interface TranslationRulesCreator<T> {
    Map<Class<? extends RmsQueryExpression>, ExpressionTranslator<T>> createTranslationRegistry();
}
