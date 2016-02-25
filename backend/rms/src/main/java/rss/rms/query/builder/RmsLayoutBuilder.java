package rss.rms.query.builder;

/**
 * @author Mark Bramnik
 *         Date: 3/21/13
 *         Time: 3:58 PM
 * @since 1.0.0-9999
 */
public interface RmsLayoutBuilder<T> {
    RmsLayoutBuilder<T> fields(String... fields);

    RmsQueryBuilder<T> done();
}
