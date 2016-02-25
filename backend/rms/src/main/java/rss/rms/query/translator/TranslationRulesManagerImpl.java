package rss.rms.query.translator;

import rss.rms.query.expression.RmsQueryExpression;

import java.util.HashMap;
import java.util.Map;

/**
 * The manager of translation - contains the registry of translation rules
 *
 * @author Mark Bramnik
 *         Date: 30/05/13
 *         Time: 15:04
 */
public class TranslationRulesManagerImpl<T> implements TranslationRulesManager<T> {
    private Map<Class<? extends RmsQueryExpression>, ExpressionTranslator<T>> translatorRegistry;

    private Map<Class<? extends RmsQueryExpression>, Class<? extends RmsQueryExpression>> cache;

    public TranslationRulesManagerImpl(TranslationRulesCreator<T> translationRulesCreator) {
        translatorRegistry = translationRulesCreator.createTranslationRegistry();
        cache = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpressionTranslator<T> getTranslator(Class<? extends RmsQueryExpression> expressionClass) {
        Class<? extends RmsQueryExpression> cl = cache.get(expressionClass);
        if (cl == null) {
            // should be lazily initialized
            cl = addMappingToCache(expressionClass);
        }
        return translatorRegistry.get(cl);
    }

    private Class<? extends RmsQueryExpression> addMappingToCache(Class<? extends RmsQueryExpression> expressionClass) {
        for (Class<? extends RmsQueryExpression> registryClass : translatorRegistry.keySet()) {
            if (registryClass.isAssignableFrom(expressionClass)) {
                cache.put(expressionClass, registryClass);
                return registryClass;
            }
        }
        throw new IllegalArgumentException("Failed to translate Expression. No assignable translation rules were found for operator of class [ " + expressionClass.getName() + "]");
    }
}
