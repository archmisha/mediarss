package rss.commons.query.api.model;

/**
 * This interface is the root of the query object model hierarchy.
 *
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public interface QueryElement {

    /**
     * Returns a deep String representation of this element.
     * <p/>
     * The returned String is a parser rule compliant representation of this expression tree.
     *
     * @return a String
     */
    String toApiString();
}
