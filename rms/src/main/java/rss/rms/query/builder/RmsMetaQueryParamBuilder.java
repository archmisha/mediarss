package rss.rms.query.builder;

/**
 * @author Mark Bramnik
 *         Date: 4/19/13
 *         Time: 11:35 AM
 * @since 1.0.0-9999
 */
public interface RmsMetaQueryParamBuilder<T> {

    RmsMetaQueryParamBuilder<T> fetchCount();

    RmsQueryBuilder<T> done();

}
