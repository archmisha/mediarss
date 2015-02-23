package rss.rms.query.builder;

import rss.rms.query.OrderInformation;
import rss.rms.query.OrderInformationImpl;

/**
 * @author Mark Bramnik
 *         Date: 4/23/13
 *         Time: 10:15 AM
 * @since 1.0.0-9999
 */
class RmsOrderBuilderImpl<T> extends RmsQueryPartSupportBuilder<T> implements RmsOrderBuilder<T> {
    private OrderInformationImpl orderInformation = new OrderInformationImpl();

    RmsOrderBuilderImpl(RmsQueryBuilderImpl<T> rmsQueryBuilder) {
        super(rmsQueryBuilder);
    }

    @Override
    public RmsOrderBuilder<T> asc(String fieldPath) {
        OrderInformation.OrderDesc orderDescriptor = new OrderInformationImpl.OrderDescImpl(fieldPath, OrderInformation.Order.ASC);
        orderInformation.addOrderDescriptor(orderDescriptor);
        return this;
    }

    @Override
    public RmsOrderBuilder<T> desc(String fieldPath) {
        OrderInformation.OrderDesc orderDescriptor = new OrderInformationImpl.OrderDescImpl(fieldPath, OrderInformation.Order.DESC);
        orderInformation.addOrderDescriptor(orderDescriptor);
        return this;
    }

    @Override
    public RmsQueryBuilder<T> done() {
        getQueryBuilder().setOrderInformation(orderInformation);
        return getQueryBuilder();
    }
}
