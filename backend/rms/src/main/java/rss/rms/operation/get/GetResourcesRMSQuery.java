package rss.rms.operation.get;

import rss.rms.operation.RmsOperation;
import rss.rms.query.RmsQueryInformation;

/**
 * @author Mark Bramnik
 *         Date: 4/22/13
 *         Time: 1:00 PM
 * @since 1.0.0-9999
 */
public interface GetResourcesRMSQuery<T> extends RmsOperation<T> {

    //    ResourceDeserializer<T> getResourceDeserializer();
    RmsQueryInformation getQueryInfo();
}
