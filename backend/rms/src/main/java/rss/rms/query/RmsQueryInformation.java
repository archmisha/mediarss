package rss.rms.query;

/**
 * @author Mark Bramnik
 *         Date: 4/22/13
 *         Time: 5:21 PM
 * @since 1.0.0-9999
 */
public interface RmsQueryInformation {
    OrderInformation getOrderInformation();

    //    PageInformation getPageInformation();
//    Bson getLayout();
    FilterInformation getFilterInformation();
//    MetadataInformation getMetadataInformation();
}
