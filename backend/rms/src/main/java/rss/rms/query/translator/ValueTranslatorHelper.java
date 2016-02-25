package rss.rms.query.translator;

import org.bson.types.ObjectId;
import rss.rms.RmsConstants;
import rss.rms.driver.MongoDriver;

/**
 * Helper class to translate the mongo id and real value
 *
 * @author Mark Bramnik
 *         Date: 24/06/13
 *         Time: 16:03
 */
public class ValueTranslatorHelper {
    public static Object translateValue(String fieldPath, Object value) {
        if (RmsConstants.RESOURCE_ID_PROPERTY_NAME.equals(fieldPath)) {
            // Handle object id
            if (value instanceof String && ObjectId.isValid((String) value)) {
                return new ObjectId((String) value);
            } else {
                throw new IllegalArgumentException("Failed to translate the query:  the supplied property for " + fieldPath + " = " + value + " is not a valid object id");
            }
        }
        return value;
    }

    public static String translateFieldName(String fieldName) {
        if (RmsConstants.RESOURCE_ID_PROPERTY_NAME.equals(fieldName)) {
            return MongoDriver.MONGO_RESOURCE_ID;
        } else {
            return fieldName;
        }
    }
}
