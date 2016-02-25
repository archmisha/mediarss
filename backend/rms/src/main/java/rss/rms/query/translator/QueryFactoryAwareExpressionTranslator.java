package rss.rms.query.translator;

import rss.commons.query.api.model.Condition;
import rss.rms.query.parser.RmsQueryElementFactory;

/**
 * @author Mark Bramnik
 *         Date: 02/06/13
 *         Time: 14:09
 */
abstract class QueryFactoryAwareExpressionTranslator implements ExpressionTranslator<Condition> {

    private RmsQueryElementFactory queryFactory;

    protected QueryFactoryAwareExpressionTranslator(RmsQueryElementFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    protected RmsQueryElementFactory getQueryFactory() {
        return queryFactory;
    }
}
