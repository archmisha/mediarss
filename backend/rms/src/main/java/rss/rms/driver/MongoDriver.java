package rss.rms.driver;

import rss.rms.RmsResource;
import rss.rms.operation.delete.DeleteResourceRMSOperation;
import rss.rms.operation.get.GetResourcesRMSQuery;

import java.util.List;

/**
 * User: dikmanm
 * Date: 18/02/2015 08:48
 */
public interface MongoDriver {
    static final String MONGO_RESOURCE_ID = "_id";

    <T extends RmsResource> T get(GetResourcesRMSQuery<T> query);

    <T extends RmsResource> List<T> getCollection(GetResourcesRMSQuery<T> query);

    <T extends RmsResource> String insert(T rmsResource, Class<T> clazz);

    <T extends RmsResource> void update(T rmsResource, Class<T> clazz);

    <T extends RmsResource> void delete(DeleteResourceRMSOperation<T> operation);

    void createDatabase();

    void dropDatabase();
}
