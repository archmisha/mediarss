package rss.rms.operation.get;

import rss.rms.query.RmsQueryInformation;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 22/05/13
 * Time: 12:22
 * To change this template use File | Settings | File Templates.
 */
public class GetResourcesRMSQueryImpl<T> implements GetResourcesRMSQuery<T> {
    //    private ResourceDeserializer<T> deserializer;
    private Class<T> resourceClass;
    private RmsQueryInformation rmsQueryInfo;


    public GetResourcesRMSQueryImpl(/*ResourceDeserializer<T> deserializer,*/ Class<T> resourceClass, RmsQueryInformation rmsQueryInfo) {
//        this.deserializer = deserializer;
        this.rmsQueryInfo = rmsQueryInfo;
        this.resourceClass = resourceClass;
    }

//    @Override
//    public ResourceDeserializer<T> getResourceDeserializer() {
//        return deserializer;
//    }

    @Override
    public Class<T> getResourceClass() {
        return resourceClass;
    }

    @Override
    public RmsQueryInformation getQueryInfo() {
        return rmsQueryInfo;
    }
}
