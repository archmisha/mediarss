package rss.rms.query.builder;

/**
 * @author Mark Bramnik
 *         Date: 4/23/13
 *         Time: 10:15 AM
 * @since 1.0.0-9999
 */
class RmsLayoutBuilderImpl<T> extends RmsQueryPartSupportBuilder<T> implements RmsLayoutBuilder<T> {
    //    private LayoutInformationImpl layoutInfo = new LayoutInformationImpl();
    RmsLayoutBuilderImpl(RmsQueryBuilderImpl<T> rmsQueryBuilder) {
        super(rmsQueryBuilder);
    }

    @Override
    public RmsLayoutBuilder<T> fields(String... fields) {

//        for(String field : fields) {
//            layoutInfo.addPath(field);
//        }

        return this;
    }

//    LayoutInformation getLayoutInformation() {
//        return layoutInfo;
//    }

    @Override
    public RmsQueryBuilder<T> done() {
//        getQueryBuilder().setLayoutInformation(layoutInfo);
        return getQueryBuilder();
    }
}
