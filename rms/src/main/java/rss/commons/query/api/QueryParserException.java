package rss.commons.query.api;

/**
 * An exception class for all query related parsing APIs.
 *
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class QueryParserException extends RuntimeException {

    public QueryParserException() {
        super();
    }

    public QueryParserException(String message) {
        super(message);
    }

    public QueryParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryParserException(Throwable cause) {
        super(cause);
    }

    protected QueryParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
