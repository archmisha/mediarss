package rss.rms.query;

import org.bson.conversions.Bson;

/**
 * @author Mark Bramnik
 *         Date: 4/22/13
 *         Time: 5:23 PM
 * @since 1.0.0-9999
 */
public interface FilterInformation {
    Bson getFilterDescriptor();
}
