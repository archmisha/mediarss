package rss.rms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.rms.driver.MongoDriver;
import rss.rms.operation.delete.DeleteResourceRMSOperation;
import rss.rms.operation.get.GetResourcesRMSQuery;

/**
 * User: dikmanm
 * Date: 16/02/2015 14:48
 */
@Service
public class ResourceManagementServiceImpl implements ResourceManagementService {

    @Autowired
    private MongoDriver mongoDriver;

    @Override
    public <T extends RmsResource> T get(GetResourcesRMSQuery<T> query) {
        return mongoDriver.get(query);
    }

    @Override
    public <T extends RmsResource> void saveOrUpdate(T rmsResource, Class<T> clazz) {
        if (rmsResource.getId() == null) {
            String id = mongoDriver.insert(rmsResource, clazz);
            rmsResource.setId(id);
        } else {
            mongoDriver.update(rmsResource, clazz);
        }
    }

    @Override
    public <T extends RmsResource> void delete(DeleteResourceRMSOperation<T> query) {
        mongoDriver.delete(query);
    }

    @Override
    public RmsOperationsFactory apiFactory() {
        return new RmsOperationsFactoryImpl();
    }
}