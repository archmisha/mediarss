package rss.rms.driver.transformer;

/**
 * Provider for common mongo db object transformers
 *
 * @author Mark Bramnik
 *         Date: 3/4/13
 *         Time: 5:07 PM
 * @since 1.0.0-9999
 */
public enum MongoObjectTransformerProvider {
    INSTANCE;
    private static final MongoObjectTransformer MONGO_ID_TO_RESOURCE_ID_OBJECT_TRANSFORMER = new MongoIdToResourceIdObjectTransformer();
    private static final MongoObjectTransformer RESOURCE_ID_TO_MONGO_ID_OBJECT_TRANSFORMER = new ResourceIdToMongoIdObjectTransformer();

    public MongoObjectTransformer getMongoIdToResourceIdObjectTransformer() {
        return MONGO_ID_TO_RESOURCE_ID_OBJECT_TRANSFORMER;
    }

    public MongoObjectTransformer getResourceIdToMongoIdObjectTransformer() {
        return RESOURCE_ID_TO_MONGO_ID_OBJECT_TRANSFORMER;
    }
}
