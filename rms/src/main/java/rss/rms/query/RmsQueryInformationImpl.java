package rss.rms.query;


/**
 * @author Mark Bramnik
 *         Date: 4/23/13
 *         Time: 1:53 PM
 * @since 1.0.0-9999
 */
public class RmsQueryInformationImpl implements RmsQueryInformation {
    private OrderInformation orderInformation;
    //    private PageInformation pageInformation;
//    private LayoutInformation layoutInformation;
    private FilterInformation filterInformation;
//    private MetadataInformation metadataInformation;

    public RmsQueryInformationImpl() {

    }

    public RmsQueryInformationImpl(RmsQueryInformation other) {
        this.orderInformation = other.getOrderInformation();
//        this.pageInformation = other.getPageInformation();
//        this.layoutInformation = other.getLayoutInformation();
        this.filterInformation = other.getFilterInformation();
//        this.metadataInformation = other.getMetadataInformation();
    }


    @Override
    public OrderInformation getOrderInformation() {
        return orderInformation;
    }

//    @Override
//    public PageInformation getPageInformation() {
//        return pageInformation;
//    }

//    @Override
//    public LayoutInformation getLayoutInformation() {
//        return layoutInformation;
//    }

    @Override
    public FilterInformation getFilterInformation() {
        return filterInformation;
    }

//    @Override
//    public MetadataInformation getMetadataInformation() {
//        return metadataInformation;
//    }

    public void setOrderInformation(OrderInformation orderInformation) {
        this.orderInformation = orderInformation;
    }

//    public void setPageInformation(PageInformation pageInformation) {
//        this.pageInformation = pageInformation;
//    }

//    public void setLayoutInformation(LayoutInformation layoutInformation) {
//        this.layoutInformation = layoutInformation;
//    }

    public void setFilterInformation(FilterInformation filterInformation) {
        this.filterInformation = filterInformation;
    }

//    public void setMetadataInformation(MetadataInformation metadataInformation) {
//        this.metadataInformation = metadataInformation;
//    }
}
