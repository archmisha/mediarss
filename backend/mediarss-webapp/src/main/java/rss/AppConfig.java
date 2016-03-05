package rss;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import rss.scheduler.SpringQuartzJobFactory;

/**
 * Created by michaeld on 05/03/2016.
 */
@Configuration
@ComponentScan(basePackages = "rss")
@PropertySources({
        @PropertySource("file:${lookup.dir}/database.properties"),
        @PropertySource("file:${lookup.dir}/mongodb.properties"),
})
@ImportResource({"classpath*:META-INF/spring/*-context.xml"})
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Autowired
    public SchedulerFactoryBean schedulerFactoryBean(SpringQuartzJobFactory springQuartzJobFactory) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(springQuartzJobFactory);
        return schedulerFactoryBean;
    }
}
