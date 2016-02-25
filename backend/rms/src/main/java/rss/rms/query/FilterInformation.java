package rss.rms.query;

import rss.rms.query.expression.RmsQueryExpression;

/**
 * @author Mark Bramnik
 *         Date: 4/22/13
 *         Time: 5:23 PM
 * @since 1.0.0-9999
 */
public interface FilterInformation {
    RmsQueryExpression getExpression();
}
