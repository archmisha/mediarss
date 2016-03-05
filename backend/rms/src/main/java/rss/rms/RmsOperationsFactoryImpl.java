package rss.rms;

import rss.rms.operation.delete.DeleteResourceRMSOperation;
import rss.rms.operation.delete.DeleteResourceRMSOperationImpl;
import rss.rms.operation.get.GetResourcesRMSQuery;
import rss.rms.operation.get.GetResourcesRMSQueryImpl;
import rss.rms.query.RmsQueryInformation;
import rss.rms.query.RmsQueryBuilder;
import rss.rms.query.RmsQueryBuilderImpl;

/**
 * @author Mark Bramnik
 *         Date: 2/17/13
 *         Time: 4:59 PM
 * @since 1.0.0-9999
 */
public class RmsOperationsFactoryImpl implements RmsOperationsFactory {

    @Override
    public <T> RmsQueryBuilder<T> createRmsQueryBuilder(/*Class<T> resourceClass*/) {
        return new RmsQueryBuilderImpl<>(/*resourceClass*/);
    }

    @Override
    public <T> GetResourcesRMSQuery<T> createGetResourceOperation(Class<T> resourceClass, RmsQueryInformation rmsQueryInformation) {
        return new GetResourcesRMSQueryImpl<>(resourceClass, rmsQueryInformation);
    }


    @Override
    public <T> DeleteResourceRMSOperation<T> createDeleteResourceOperation(Class<T> resourceClass, RmsQueryInformation rmsQueryInformation) {
        return new DeleteResourceRMSOperationImpl<>(resourceClass, rmsQueryInformation.getFilterInformation());
    }

//    @Override
//    public <T> InsertResourceRMSOperation<T> createInsertResourceOperation(String resourceType, T resource) {
//        return new InsertResourceRMSOperationImpl<>(resourceType, resource);
//    }
//
//    @Override
//    public <T> InsertResourceRMSOperation<T> createInsertResourceOperation(String resourceType, T resource, ResourceSerializer<T> resourceSerializer) {
//        return new InsertResourceRMSOperationImpl<>(resourceType, resource, resourceSerializer);
//    }
//
//    @Override
//    public <T> BulkBuilder<T> createBulkBuilder() {
//        return new BulkBuilderImpl<>();
//    }
//
//    @Override
//    public <T> UpdateResourceRMSOperation<T> createUpdateResourceOperation(String resourceType, T resource) {
//        return new UpdateResourceRMSOperationImpl<>(resourceType, resource);
//    }
//
//    @Override
//    public <T> UpdateResourceRMSOperation<T> createUpdateResourceOperation(String resourceType, T resource, ResourceSerializer<T> resourceSerializer) {
//        return new UpdateResourceRMSOperationImpl<>(resourceType, resource, resourceSerializer);
//    }
//
//    @Override
//    public <T> BulkResourceRMSOperation<T> createBulkResourceOperation(String resourceType, Bulk<T> bulk, ResourceSerializer<T> resourceSerializer) {
//        return new BulkResourceRMSOperationImpl<>(resourceType, bulk, resourceSerializer);
//    }
//
//    @Override
//    public <T> BulkResourceRMSOperation<T> createBulkResourceOperation(String resourceType, Bulk<T> bulk) {
//        return new BulkResourceRMSOperationImpl<>(resourceType, bulk);
//    }

}
