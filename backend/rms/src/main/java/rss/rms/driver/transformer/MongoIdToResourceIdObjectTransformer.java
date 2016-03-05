package rss.rms.driver.transformer;

import org.bson.Document;
import org.bson.types.ObjectId;
import rss.rms.RmsConstants;
import rss.rms.driver.MongoDriver;

/**
 * Converts a mongo object with objectId specified as "_id" to a resource with id specified as "id" .
 * Changes a state of the input object and returns it back
 *
 * @author Mark Bramnik
 *         Date: 3/4/13
 *         Time: 4:25 PM
 * @since 1.0.0-9999
 */
public class MongoIdToResourceIdObjectTransformer implements MongoObjectTransformer {
    MongoIdToResourceIdObjectTransformer() {
    }

    @Override
    public Document transform(Document inObject) {
        if (inObject.containsKey(MongoDriver.MONGO_RESOURCE_ID)) {
            ObjectId objectId = (ObjectId) inObject.remove(MongoDriver.MONGO_RESOURCE_ID);
            inObject.put(RmsConstants.RESOURCE_ID_PROPERTY_NAME, objectId.toString());
        }

        return inObject;
    }
}
