package rss.rms.query.builder;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 22/05/13
 * Time: 09:38
 * To change this template use File | Settings | File Templates.
 */
abstract class ExpressionBuilderContextManagerSupport<T> {

    private FilterBuilderContextManager<T> ctx;

    protected ExpressionBuilderContextManagerSupport(FilterBuilderContextManager<T> ctx) {
        this.ctx = ctx;
    }

    protected final FilterBuilderContextManager<T> getContext() {
        return ctx;
    }
}
