package rss.rms.driver.transformer;

import org.bson.Document;

/**
 * Defines a contract for transformation of db objects at the mongo driver level
 *
 * @author Mark Bramnik
 *         Date: 3/4/13
 *         Time: 4:21 PM
 * @since 1.0.0-9999
 */
public interface MongoObjectTransformer {
    Document transform(Document inObject);
}
