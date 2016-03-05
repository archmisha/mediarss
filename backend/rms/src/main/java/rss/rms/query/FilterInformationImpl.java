package rss.rms.query;

import org.bson.conversions.Bson;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class FilterInformationImpl implements FilterInformation {

    private Bson filterDescriptor;

    public FilterInformationImpl(Bson filterDescriptor) {
        this.filterDescriptor = filterDescriptor;
    }

    @Override
    public Bson getFilterDescriptor() {
        return filterDescriptor;
    }
}
