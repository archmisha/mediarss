package rss.rms.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 12/05/13
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class OrderInformationImpl implements OrderInformation {

    public static class OrderDescImpl implements OrderDesc {
        private String path;
        private Order order;

        public OrderDescImpl(String path, Order order) {
            this.path = path;
            this.order = order;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public Order getOrder() {
            return order;
        }
    }

    private List<OrderDesc> orderDescriptorsList;

    public OrderInformationImpl() {
        this.orderDescriptorsList = new ArrayList<>();
    }


    public void addOrderDescriptor(OrderDesc orderDescriptor) {

        this.orderDescriptorsList.add(orderDescriptor);
    }

    @Override
    public List<OrderDesc> getOrderDescriptors() {
        return orderDescriptorsList;
    }
}
