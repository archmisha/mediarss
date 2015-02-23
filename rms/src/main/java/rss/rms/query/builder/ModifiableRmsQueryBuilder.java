package rss.rms.query.builder;

import rss.rms.query.FilterInformation;
import rss.rms.query.OrderInformation;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 22/05/13
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
interface ModifiableRmsQueryBuilder<T> extends RmsQueryBuilder<T> {

    //    void setPageInformation(PageInformation pageInfo);
//    void setLayoutInformation(LayoutInformation layoutInfo);
    void setOrderInformation(OrderInformation orderInfo);

    void setFilterInformation(FilterInformation pageInfo);
//    void setMetadataInformation(MetadataInformation metadataInfo);
}
