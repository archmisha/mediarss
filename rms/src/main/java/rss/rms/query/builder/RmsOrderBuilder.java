package rss.rms.query.builder;

/**
 * @author Mark Bramnik
 *         Date: 3/24/13
 *         Time: 11:34 AM
 * @since 1.0.0-9999
 */
public interface RmsOrderBuilder<T> {
    RmsOrderBuilder<T> asc(String path);

    RmsOrderBuilder<T> desc(String path);

    RmsQueryBuilder<T> done();
}
