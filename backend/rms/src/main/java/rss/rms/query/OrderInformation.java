package rss.rms.query;

import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author Mark Bramnik
 *         Date: 4/22/13
 *         Time: 5:22 PM
 * @since 1.0.0-9999
 */
public interface OrderInformation {

    List<Bson> getOrderDescriptors();
}
