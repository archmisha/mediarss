package rss.rms.query;

import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class OrderInformationImpl implements OrderInformation {

    private List<Bson> orderDescriptorsList;

    public OrderInformationImpl(Bson ... orderDescriptors) {
        this.orderDescriptorsList = new ArrayList<>();
        this.orderDescriptorsList.addAll(Arrays.asList(orderDescriptors));
    }

    public void addOrderDescriptor(Bson orderDescriptor) {
        this.orderDescriptorsList.add(orderDescriptor);
    }

    @Override
    public List<Bson> getOrderDescriptors() {
        return orderDescriptorsList;
    }
}
