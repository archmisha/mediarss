package rss.rms.driver;

import com.mongodb.Block;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rss.rms.RmsResource;
import rss.rms.driver.transformer.MongoObjectTransformerProvider;
import rss.rms.operation.delete.DeleteResourceRMSOperation;
import rss.rms.operation.get.GetResourcesRMSQuery;
import rss.rms.query.FilterInformation;
import rss.rms.query.RmsQueryInformation;
import rss.util.JsonTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 18/02/2015 08:48
 */
@Service
public class MongoDriverImpl implements MongoDriver {

    @Autowired
    private MongoClient mongoClient;

    @Value("${mongodb.database}")
    private String database;

    @Override
    public void createDatabase() {
        mongoClient.getDatabase(database);
    }

    @Override
    public void dropDatabase() {
        mongoClient.dropDatabase(database);
    }

    @Override
    public <T extends RmsResource> T get(GetResourcesRMSQuery<T> query) {
        RmsQueryInformation queryInfo = query.getQueryInfo();
        MongoCollection<Document> dbCollection = getDbCollection(query.getResourceClass());
        FindIterable<Document> dbCursor = dbCollection.find(queryInfo.getFilterInformation().getFilterDescriptor());
//        dbCursor.projection(queryInfo.getLayout());
        if (queryInfo.getOrderInformation() != null) {
            dbCursor.sort(Sorts.orderBy(queryInfo.getOrderInformation().getOrderDescriptors()));
        }
        final List<Document> result = new ArrayList<>();
        dbCursor.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                result.add(document);
            }
        });
        if (result.isEmpty()) {
            return null;
        }
        return dbObjectToObject(query.getResourceClass(), result.get(0));
    }

    @Override
    public <T extends RmsResource> List<T> getCollection(final GetResourcesRMSQuery<T> query) {
        RmsQueryInformation queryInfo = query.getQueryInfo();
        MongoCollection<Document> dbCollection = getDbCollection(query.getResourceClass());
        FindIterable<Document> dbCursor = dbCollection.find(queryInfo.getFilterInformation().getFilterDescriptor());
//        dbCursor.projection(queryInfo.getLayout());

        final List<T> result = new ArrayList<>();
        dbCursor.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                result.add(dbObjectToObject(query.getResourceClass(), document));
            }
        });
        return result;
    }

    @Override
    public <T extends RmsResource> String insert(T rmsResource, Class<T> clazz) {
        MongoCollection<Document> dbCollection = getDbCollection(clazz);
        Document dbObject = objectToDbObject(rmsResource);
        dbCollection.insertOne(dbObject);
        return dbObject.get(MongoDriver.MONGO_RESOURCE_ID).toString();
    }

    @Override
    public <T extends RmsResource> void update(T rmsResource, Class<T> clazz) {
        MongoCollection<Document> dbCollection = getDbCollection(clazz);
        Document dbObject = objectToDbObject(rmsResource);
        dbCollection.replaceOne(queryById(getObjectId(dbObject)), dbObject);
    }

    @Override
    public <T extends RmsResource> void delete(DeleteResourceRMSOperation<T> operation) {
        FilterInformation filterInfo = operation.getFilterInformation();
        MongoCollection<Document> dbCollection = getDbCollection(operation.getResourceClass());
        dbCollection.deleteMany(filterInfo.getFilterDescriptor());
    }

    private <T extends RmsResource> Document objectToDbObject(T rmsResource) {
        Document dbObject = new Document(objectToMap(rmsResource));
        dbObject = MongoObjectTransformerProvider.INSTANCE.getResourceIdToMongoIdObjectTransformer().transform(dbObject);
        return dbObject;
    }

    private <T extends RmsResource> T dbObjectToObject(Class<T> clazz, Document dbObject) {
        dbObject = MongoObjectTransformerProvider.INSTANCE.getMongoIdToResourceIdObjectTransformer().transform(dbObject);
        String dbObjectJson = JSON.serialize(dbObject);
        return JsonTranslation.jsonString2Object(dbObjectJson, clazz);
    }

    private <T extends RmsResource> MongoCollection<Document> getDbCollection(Class<T> clazz) {
        MongoDatabase db = getDb();
        return db.getCollection(clazz.getSimpleName());
    }

    private MongoDatabase getDb() {
        return mongoClient.getDatabase(database);
    }

    private <T extends RmsResource> Map objectToMap(T obj) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        return mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Helper method used to return a mongo query that locates a resource with the given id.
     *
     * @param objectId The id of the resource to query by
     * @return A {@link DBObject} representing a query by id
     */
    private Document queryById(ObjectId objectId) {
        return new Document(MongoDriver.MONGO_RESOURCE_ID, objectId);
    }

    private ObjectId getObjectId(Document object) {
        return (ObjectId) object.get(MongoDriver.MONGO_RESOURCE_ID);
    }
}
