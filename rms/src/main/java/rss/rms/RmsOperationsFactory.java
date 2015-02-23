package rss.rms;

import rss.rms.operation.delete.DeleteResourceRMSOperation;
import rss.rms.operation.get.GetResourcesRMSQuery;
import rss.rms.query.RmsQueryInformation;
import rss.rms.query.builder.RmsQueryBuilder;

/**
 * Factory for generating parameters for use in {@link RmsService}, obtained by calling {@link RmsService#apiFactory()}.
 *
 * @author Mark Bramnik
 *         Date: 2/13/13
 *         Time: 10:05 PM
 * @since 1.0.0-9999
 */
public interface RmsOperationsFactory {

    <T> RmsQueryBuilder<T> createRmsQueryBuilder(/*Class<T> resourceClass*/);

    <T> GetResourcesRMSQuery<T> createGetResourceOperation(Class<T> resourceClass, RmsQueryInformation rmsQueryInformation);

    <T> DeleteResourceRMSOperation<T> createDeleteResourceOperation(Class<T> resourceClass, RmsQueryInformation rmsQueryInformation);

//    <T> InsertResourceRMSOperation<T> createInsertResourceOperation(String resourceType, T resource);
//
//    <T> InsertResourceRMSOperation<T> createInsertResourceOperation(String resourceType, T resource, ResourceSerializer<T> resourceSerializer);
//
//    <T> BulkBuilder<T> createBulkBuilder();
//
//    <T> BulkResourceRMSOperation<T> createBulkResourceOperation(String resourceType, Bulk<T> bulk, ResourceSerializer<T> resourceSerializer);
//
//    <T> BulkResourceRMSOperation<T> createBulkResourceOperation(String resourceType, Bulk<T> bulk);
//
//    <T> UpdateResourceRMSOperation<T> createUpdateResourceOperation(String resourceType, T resource);
//
//
//    <T> UpdateResourceRMSOperation<T> createUpdateResourceOperation(String resourceType, T resource, ResourceSerializer<T> resourceSerializer);

}
