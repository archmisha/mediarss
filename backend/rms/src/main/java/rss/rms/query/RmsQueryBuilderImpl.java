package rss.rms.query;

import org.bson.conversions.Bson;

/**
 * @author Mark Bramnik
 *         Date: 4/23/13
 *         Time: 10:10 AM
 * @since 1.0.0-9999
 */
public class RmsQueryBuilderImpl<T> implements RmsQueryBuilder<T> {
    //    private ResourceDeserializer<T> deserializer;
    private RmsQueryInformationImpl rmsQueryInformation = new RmsQueryInformationImpl();
    private boolean fetchCount = false;
    private Integer pageSize = null;
    private Integer pageSkip = null;

//    private String resourceType;
//    private Class<T> resourceClass;

    public RmsQueryBuilderImpl(/*String resourceType,*//* Class<T> resourceClass*/) {
//        this.resourceType = resourceType;
//        this.resourceClass = resourceClass;
    }

    @Override
    public RmsQueryBuilderImpl<T> order(Bson ... orderDescriptors) {
        rmsQueryInformation.setOrderInformation(new OrderInformationImpl(orderDescriptors));
        return this;
    }


    @Override
    public RmsQueryBuilder<T> filter(Bson filter) {
        rmsQueryInformation.setFilterInformation(new FilterInformationImpl(filter));
        return this;
    }
//
//    @Override
//    public RmsQueryBuilderImpl<T> layout() {
//        return new RmsLayoutBuilderImpl<>(this);
//    }


//    @Override
//    public RmsQueryBuilder<T> deserializer(ResourceDeserializer<T> deserializer) {
//        this.deserializer= deserializer;
//        return this;
//    }

    @Override
    public RmsQueryBuilder<T> fetchCount(boolean fetchCount) {
        this.fetchCount = fetchCount;
        return this;
    }

    @Override
    public RmsQueryBuilder<T> pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public RmsQueryBuilder<T> skip(Integer skip) {
        this.pageSkip = skip;
        return this;
    }

    @Override
    public RmsQueryInformation getRmsQueryInformation() {
        return rmsQueryInformation;
    }

//    @Override
//    public GetResourcesRMSQuery<T> buildQuery() {
//        // setup the page information
//        if(pageSize != null || pageSkip != null) {
//            PageInformation pageInfo = new PageInformationImpl(pageSize, pageSkip);
//            setPageInformation(pageInfo);
//        }
//
//        // setup the metadataInformation
//
//        MetadataInformationImpl metadataInfo = new MetadataInformationImpl();
//        metadataInfo.setFetchCount(this.fetchCount);
//        setMetadataInformation(metadataInfo);
//
//        RmsQueryInformationImpl queryInformationCopy = new RmsQueryInformationImpl(rmsQueryInformation);
//        return new GetResourcesRMSQueryImpl<>(queryInformationCopy, resourceClass);
//    }

//    @Override
//    public DeleteResourceRMSOperation<T> buildDeleteQuery() {
//        RmsQueryInformationImpl queryInformationCopy = new RmsQueryInformationImpl(rmsQueryInformation);
//        return new DeleteResourceRMSOperationImpl<>(queryInformationCopy.getFilterInformation(), resourceClass);
//    }

//    @Override
//    public void setPageInformation(PageInformation pageInfo) {
//        rmsQueryInformation.setPageInformation(pageInfo);
//    }
//
//    @Override
//    public void setLayoutInformation(LayoutInformation layoutInfo) {
//        rmsQueryInformation.setLayoutInformation(layoutInfo);
//    }
//
//    @Override
//    public void setOrderInformation(OrderInformation orderInfo) {
//        rmsQueryInformation.setOrderInformation(orderInfo);
//    }
//
//    @Override
//    public void setFilterInformation(FilterInformation pageInfo) {
//        rmsQueryInformation.setFilterInformation(pageInfo);
//    }

//    @Override
//    public void setMetadataInformation(MetadataInformation metadataInfo) {
//        rmsQueryInformation.setMetadataInformation(metadataInfo);
//    }
}
