package rss.rms.operation;

/**
 * @author Mark Bramnik
 *         Date: 2/14/13
 *         Time: 9:17 AM
 * @since 1.0.0-9999
 */
public interface RmsOperation<T> {
    Class<T> getResourceClass();
}
