package rss.rms.query.translator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import rss.rms.RmsConstants;
import rss.rms.driver.MongoDriver;
import rss.rms.query.FilterInformation;
import rss.rms.query.OrderInformation;
import rss.rms.query.RmsQueryInformation;
import rss.rms.query.expression.RmsQueryExpression;

/**
 * This class is responsible for the translation of RmsQueryInformation object to the Mongo DB specific query objects.
 *
 * @author Mark Bramnik
 */
public class MongoDbQueryTranslator {
    private static TranslationRulesManager<DBObject> translationRulesManager = new TranslationRulesManagerImpl<>(new MongoTranslationRulesCreator());
    private static final Integer ASC_ORDER_INT = 1;
    private static final Integer DESC_ORDER_INT = -1;
    private static final Integer LAYOUT_INT = 1;

    public MongoDbQueryTranslationResult translateQuery(RmsQueryInformation queryInformation) {
        DBObject filter = translateFilter(queryInformation.getFilterInformation());
//        DBObject layout = translateLayout(dalQuery.getLayoutInformation());
        DBObject order = translateOrder(queryInformation.getOrderInformation());
        return new MongoDbQueryTranslationResult(filter, null/*layout*/, order);
    }

    public DBObject translateFilter(FilterInformation filterInformation) {
        if (filterInformation == null) {
            return null;
        }
        RmsQueryExpression exp = filterInformation.getExpression();
        ExpressionTranslator<DBObject> translator = translationRulesManager.getTranslator(exp.getClass());
        return translator.translateExpression(exp, translationRulesManager);
    }

//    private DBObject translateLayout(LayoutInformation layoutInformation) {
//        if(layoutInformation == null){
//           return null;
//        }
//        BasicDBObject layout = new BasicDBObject();
//        for(String path : layoutInformation.getPaths()) {
//            if(path.equals(RmsConstants.RESOURCE_ID_PROPERTY_NAME)) {
//                layout.put(MongoDriver.MONGO_RESOURCE_ID, LAYOUT_INT);
//            }
//            else {
//                layout.put(path, LAYOUT_INT);
//            }
//        }
//        return layout;
//    }

    private DBObject translateOrder(OrderInformation orderInformation) {
        if (orderInformation == null) {
            return null;
        }
        BasicDBObject order = new BasicDBObject();
        for (OrderInformation.OrderDesc orderDesc : orderInformation.getOrderDescriptors()) {
            Integer sortingOrder = (orderDesc.getOrder() == OrderInformation.Order.ASC ? ASC_ORDER_INT : DESC_ORDER_INT);

            if (orderDesc.getPath().equals(RmsConstants.RESOURCE_ID_PROPERTY_NAME)) {
                order.put(MongoDriver.MONGO_RESOURCE_ID, sortingOrder);
            } else {
                order.put(orderDesc.getPath(), sortingOrder);
            }

        }
        return order;
    }
}