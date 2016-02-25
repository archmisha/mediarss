package rss.rms.query.translator;

import com.mongodb.DBObject;

/**
 * Contains information about the Query in mongo db representation
 *
 * @author Mark Bramnik
 */
public final class MongoDbQueryTranslationResult {
    private DBObject filter;
    private DBObject layout;
    private DBObject order;

    public MongoDbQueryTranslationResult(DBObject filter, DBObject layout, DBObject order) {
        this.filter = filter;
        this.layout = layout;
        this.order = order;
    }

    public DBObject getFilter() {
        return filter;
    }

    public DBObject getOrder() {
        return order;
    }

    public DBObject getLayout() {
        return layout;
    }
}
