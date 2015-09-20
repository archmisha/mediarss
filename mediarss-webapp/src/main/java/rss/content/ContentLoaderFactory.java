package rss.content;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import rss.environment.Environment;

import javax.annotation.Resource;

/**
 * User: dikmanm
 * Date: 16/08/2015 23:37
 */
@Component
@Primary
public class ContentLoaderFactory implements FactoryBean<ContentLoader> {

    @Resource
    private DevContentLoader devContentLoader;

    @Resource
    private ProdContentLoader prodContentLoader;

    @Resource
    private TestContentLoader testContentLoader;

    @Override
    public ContentLoader getObject() throws Exception {
        switch (Environment.getInstance().getServerMode()) {
            case TEST:
                return testContentLoader;
            case DEV:
                return devContentLoader;
            default:
                return prodContentLoader;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return ContentLoader.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
