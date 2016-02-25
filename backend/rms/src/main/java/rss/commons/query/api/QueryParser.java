package rss.commons.query.api;

import rss.commons.query.api.model.*;

/**
 * This interface represents a query parser. The parser is capable of parsing any of the defined query sections:
 * <ul>
 * <li>{@link QueryLayout}</li>
 * <li>{@link QueryFilter}</li>
 * <li>{@link QueryOrder}</li>
 * <li>{@link QueryPage}</li>
 * <li>{@link QueryMeta}</li>
 * </ul>
 * <p/>
 * Usage example:
 * <pre>
 *     // Construct a query factory instance.
 *     QueryParserFactory factory = new QueryParserFactory();
 *
 *     // Obtain a parser instance.
 *     QueryParser parser = factory.getParser();
 *
 *     QueryLayout layout = parser.parseLayout("Count(), Id, Name, Description");
 *     QueryFilter filter = parser.parseFilter("Name eq 'Jake'");
 *     QueryOrder order = parser.parseOrder("Count() desc, Id");
 *     QueryPage page = parser.parsePage("100, 20");
 *     QueryMeta meta = parser.parseMeta("Count.Total, Count.Page");
 *
 *
 * </pre>
 *
 * @author shai.nagar@hp.com
 *         Date: 3/13/13
 * @see QueryParserFactory
 */
public interface QueryParser {


    /**
     * Parses a query layout expression into an object tree.
     *
     * @param layoutExpression a query layout expression.
     * @return a {@link QueryLayout}
     */
    QueryLayout parseLayout(String layoutExpression);

    /**
     * Parses a query group expression into an object tree.
     *
     * @param groupExpression a query group expression.
     * @return a {@link QueryGroup}
     */
    public QueryGroup parseGroup(String groupExpression);

    /**
     * Parses a query filter expression into an object tree.
     *
     * @param filterExpression a query filter expression.
     * @return a {@link QueryFilter}
     */
    QueryFilter parseFilter(String filterExpression);

    /**
     * Parses a query order expression into an object tree.
     *
     * @param orderExpression a query order expression.
     * @return a {@link QueryOrder}
     */
    QueryOrder parseOrder(String orderExpression);

    /**
     * Parses a query page expression into an object tree.
     *
     * @param pageExpression a query page expression.
     * @return a {@link QueryPage}
     * @deprecated Use {@link #parseSize(String)} and {@link #parseSkip(String)} instead.
     */
    @Deprecated
    QueryPage parsePage(String pageExpression);

    /**
     * Parses a query skip expression (positive integer) into an object.
     *
     * @param skipNumberExpression a number expression representing the skip.
     * @return a {@link QueryPage}
     */
    int parseSkip(String skipNumberExpression);

    /**
     * Parses a query size expression (positive integer) into an object.
     *
     * @param sizeNumberExpression a number expression representing the size.
     * @return a {@link QueryPage}
     */
    int parseSize(String sizeNumberExpression);

    /**
     * Parses a query meta expression into an object tree.
     *
     * @param metaExpression a query "meta" expression.
     * @return a {@link QueryMeta}
     */
    QueryMeta parseMeta(String metaExpression);
}
