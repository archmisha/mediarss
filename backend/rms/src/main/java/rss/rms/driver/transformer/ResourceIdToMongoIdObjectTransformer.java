package rss.rms.driver.transformer;

import org.bson.Document;
import org.bson.types.ObjectId;
import rss.rms.RmsConstants;
import rss.rms.driver.MongoDriver;

/**
 * Converts a resource with id specified as "id" to mongo id specified as "_id" so that the object id class is created.
 * Changes a state of the input object and returns it back
 *
 * @author Mark Bramnik
 *         Date: 3/4/13
 *         Time: 4:22 PM
 * @since 1.0.0-9999
 */
public class ResourceIdToMongoIdObjectTransformer implements MongoObjectTransformer {
    ResourceIdToMongoIdObjectTransformer() {
    }

    @Override
    public Document transform(Document inObject) throws InvalidObjectIdException {
        boolean idExists = inObject.containsKey(RmsConstants.RESOURCE_ID_PROPERTY_NAME);
        String objectId = (String) inObject.remove(RmsConstants.RESOURCE_ID_PROPERTY_NAME);
        if (idExists) {
            if (ObjectId.isValid(objectId)) {
                inObject.put(MongoDriver.MONGO_RESOURCE_ID, new ObjectId(objectId));
            } else {
                throw new InvalidObjectIdException(objectId);
            }
        }
        return inObject;
    }
}
