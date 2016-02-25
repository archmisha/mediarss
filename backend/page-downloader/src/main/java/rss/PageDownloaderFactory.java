package rss;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import rss.environment.Environment;
import rss.environment.ServerMode;

import javax.annotation.Resource;

/**
 * User: dikmanm
 * Date: 16/08/2015 17:27
 */
@Component
@Primary
public class PageDownloaderFactory implements FactoryBean<PageDownloader> {

    @Resource
    private PageDownloaderImpl pageDownloader;

    @Resource
    private TestPageDownloaderImpl testPageDownloader;

    @Override
    public PageDownloader getObject() throws Exception {
        if (Environment.getInstance().getServerMode() == ServerMode.TEST) {
            return testPageDownloader;
        }
        return pageDownloader;
    }

    @Override
    public Class<?> getObjectType() {
        return PageDownloader.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}