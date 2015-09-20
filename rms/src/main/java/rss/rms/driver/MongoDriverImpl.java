package rss.rms.driver;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;
import rss.environment.Environment;
import rss.rms.RmsResource;
import rss.rms.driver.transformer.MongoObjectTransformerProvider;
import rss.rms.operation.delete.DeleteResourceRMSOperation;
import rss.rms.operation.get.GetResourcesRMSQuery;
import rss.rms.query.translator.MongoDbQueryTranslationResult;
import rss.rms.query.translator.MongoDbQueryTranslator;
import rss.util.JsonTranslation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.UnknownHostException;
import java.util.*;

/**
 * User: dikmanm
 * Date: 18/02/2015 08:48
 */
@Service
public class MongoDriverImpl implements MongoDriver {

    private MongoClient mongoClient;
    private String database;

    @PostConstruct
    private void postConstruct() {
        try {
            Properties props = Environment.getInstance().lookup("mongodb.properties");

            String hostname = props.getProperty("mongodb.host");
            Integer port = Integer.valueOf(props.getProperty("mongodb.port"));
            database = props.getProperty("mongodb.database");
            String username = props.getProperty("mongodb.username");
            String password = props.getProperty("mongodb.password");

            MongoCredential credential = MongoCredential.createMongoCRCredential(username, "admin", password.toCharArray());
            mongoClient = new MongoClient(new ServerAddress(hostname, port), Arrays.asList(credential));
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed initializing a connection to the mongodb server: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    private void preDestroy() {
        mongoClient.close();
    }

    @Override
    public <T extends RmsResource> T get(GetResourcesRMSQuery<T> query) {
        MongoDbQueryTranslator queryTranslator = new MongoDbQueryTranslator();
        MongoDbQueryTranslationResult queryTranslationResult = queryTranslator.translateQuery(query.getQueryInfo()/*resourceQueryContainer.getDalQuery()*/);

        DBCollection dbCollection = getDbCollection(query.getResourceClass());
        DBObject dbObject = dbCollection.findOne(queryTranslationResult.getFilter(), queryTranslationResult.getLayout(), queryTranslationResult.getOrder());
        if (dbObject == null) {
            return null;
        }
        return dbObjectToObject(query.getResourceClass(), dbObject);
    }

    @Override
    public <T extends RmsResource> List<T> getCollection(GetResourcesRMSQuery<T> query) {
        MongoDbQueryTranslator queryTranslator = new MongoDbQueryTranslator();
        MongoDbQueryTranslationResult queryTranslationResult = queryTranslator.translateQuery(query.getQueryInfo()/*resourceQueryContainer.getDalQuery()*/);

        List<T> result = new ArrayList<>();
        DBCollection dbCollection = getDbCollection(query.getResourceClass());
        DBCursor dbCursor = dbCollection.find(queryTranslationResult.getFilter(), queryTranslationResult.getLayout());
        while (dbCursor.hasNext()) {
            DBObject dbObject = dbCursor.next();
            result.add(dbObjectToObject(query.getResourceClass(), dbObject));
        }

        return result;
    }

    @Override
    public <T extends RmsResource> String insert(T rmsResource, Class<T> clazz) {
        DBCollection dbCollection = getDbCollection(clazz);
        DBObject dbObject = objectToDbObject(rmsResource);
        dbCollection.insert(dbObject);
        return dbObject.get(MongoDriver.MONGO_RESOURCE_ID).toString();
    }

    @Override
    public <T extends RmsResource> void update(T rmsResource, Class<T> clazz) {
        DBCollection dbCollection = getDbCollection(clazz);
        DBObject dbObject = objectToDbObject(rmsResource);
        dbCollection.update(queryById(getObjectId(dbObject)), dbObject);
    }

    @Override
    public <T extends RmsResource> void delete(DeleteResourceRMSOperation<T> operation) {
        MongoDbQueryTranslator queryTranslator = new MongoDbQueryTranslator();
        DBObject filterTranslationResult = queryTranslator.translateFilter(operation.getFilterInformation());

        DBCollection dbCollection = getDbCollection(operation.getResourceClass());
        dbCollection.remove(filterTranslationResult);
    }

    private <T extends RmsResource> DBObject objectToDbObject(T rmsResource) {
        DBObject dbObject = new BasicDBObject(objectToMap(rmsResource));
        dbObject = MongoObjectTransformerProvider.INSTANCE.getResourceIdToMongoIdObjectTransformer().transform(dbObject);
        return dbObject;
    }

    private <T extends RmsResource> T dbObjectToObject(Class<T> traktAuthJsonClass, DBObject dbObject) {
        dbObject = MongoObjectTransformerProvider.INSTANCE.getMongoIdToResourceIdObjectTransformer().transform(dbObject);
        String dbObjectJson = JSON.serialize(dbObject);
        return JsonTranslation.jsonString2Object(dbObjectJson, traktAuthJsonClass);
    }

    private <T extends RmsResource> DBCollection getDbCollection(Class<T> traktAuthJsonClass) {
        DB db = getDb();
        return db.getCollection(traktAuthJsonClass.getSimpleName());
    }

    private DB getDb() {
        return mongoClient.getDB(database);
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
    private DBObject queryById(ObjectId objectId) {
        return new BasicDBObject(MongoDriver.MONGO_RESOURCE_ID, objectId);
    }

    private ObjectId getObjectId(DBObject object) {
        return (ObjectId) object.get(MongoDriver.MONGO_RESOURCE_ID);
    }
}
