package rss.rms.operation.delete;

import rss.rms.operation.RmsOperation;
import rss.rms.query.FilterInformation;

/**
 * @author Mark Bramnik
 *         Date: 2/13/13
 *         Time: 9:37 PM
 * @since 1.0.0-9999
 */
public interface DeleteResourceRMSOperation<T> extends RmsOperation<T> {
    FilterInformation getFilterInformation();
}
