package rss.rms.query.builder;

/**
 * @author Mark Bramnik
 *         Date: 4/23/13
 *         Time: 10:21 AM
 * @since 1.0.0-9999
 */
abstract class RmsQueryPartSupportBuilder<T> {
    private RmsQueryBuilderImpl<T> rmsQueryBuilder;

    protected RmsQueryPartSupportBuilder(RmsQueryBuilderImpl<T> rmsQueryBuilder) {
        this.rmsQueryBuilder = rmsQueryBuilder;
    }

    protected RmsQueryBuilderImpl<T> getQueryBuilder() {
        return this.rmsQueryBuilder;
    }
}
