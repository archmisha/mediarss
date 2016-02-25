package rss.rms.query;

import java.util.List;

/**
 * @author Mark Bramnik
 *         Date: 4/22/13
 *         Time: 5:22 PM
 * @since 1.0.0-9999
 */
public interface OrderInformation {
    static enum Order {
        ASC, DESC
    }

    static interface OrderDesc {
        String getPath();

        Order getOrder();
    }

    List<OrderDesc> getOrderDescriptors();
}
