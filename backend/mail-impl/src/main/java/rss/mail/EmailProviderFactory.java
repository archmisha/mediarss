package rss.mail;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import rss.environment.Environment;
import rss.environment.ServerMode;

import javax.annotation.Resource;

/**
 * User: dikmanm
 * Date: 16/08/2015 23:24
 */
@Component
@Primary
public class EmailProviderFactory implements FactoryBean<EmailProvider> {

    @Resource
    private TestEmailProvider testEmailProvider;

    @Resource
    private GoogleMailProvider googleMailProvider;

    @Override
    public EmailProvider getObject() throws Exception {
        if (Environment.getInstance().getServerMode() == ServerMode.TEST) {
            return testEmailProvider;
        }
        return googleMailProvider;
    }

    @Override
    public Class<?> getObjectType() {
        return EmailProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
