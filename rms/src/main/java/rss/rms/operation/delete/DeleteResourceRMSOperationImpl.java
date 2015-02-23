package rss.rms.operation.delete;

import rss.rms.query.FilterInformation;

/**
 * Implementation of Delete operation for the RMS module
 *
 * @author Mark Bramnik
 *         Date: 2/17/13
 *         Time: 5:01 PM
 * @since 1.0.0-9999
 */
public class DeleteResourceRMSOperationImpl<T> implements DeleteResourceRMSOperation<T> {
    private Class<T> resourceClass;
    private FilterInformation filterInformation;

    public DeleteResourceRMSOperationImpl(Class<T> resourceClass, FilterInformation filterInformation) {
        this.resourceClass = resourceClass;
        this.filterInformation = filterInformation;
    }

    @Override
    public Class<T> getResourceClass() {
        return resourceClass;
    }

    public FilterInformation getFilterInformation() {
        return filterInformation;
    }
}
