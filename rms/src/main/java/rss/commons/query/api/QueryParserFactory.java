package rss.commons.query.api;

/**
 * This class provides factory method for {@link com.hp.maas.platform.commons.query.api.QueryParser} instances.
 * <p/>
 * Usage Sample:
 * <pre>
 *     // Construct a query factory instance.
 *     QueryParserFactory factory = new QueryParserFactory();
 *
 *     // Obtain a parser instance.
 *     QueryParser parser = factory.getParser();
 * </pre>
 *
 * @author shai.nagar@hp.com
 *         Date: 3/13/13
 */
public class QueryParserFactory {

    /**
     * Returns an instance of {@link com.hp.maas.platform.commons.query.api.QueryParser}.
     *
     * @return an instance of {@link com.hp.maas.platform.commons.query.api.QueryParser}.
     */
    public final QueryParser getParser() {
        return new QueryParserImpl();
    }

}
