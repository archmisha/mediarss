package rss.rms;

import rss.rms.operation.delete.DeleteResourceRMSOperation;
import rss.rms.operation.get.GetResourcesRMSQuery;

import java.util.List;

/**
 * User: dikmanm
 * Date: 16/02/2015 14:48
 */
public interface ResourceManagementService {
    <T extends RmsResource> T get(GetResourcesRMSQuery<T> query);

    <T extends RmsResource> List<T> getCollection(GetResourcesRMSQuery<T> query);

    <T extends RmsResource> void saveOrUpdate(T rmsResource, Class<T> clazz);

    <T extends RmsResource> void delete(DeleteResourceRMSOperation<T> query);

    RmsOperationsFactory apiFactory();
}
