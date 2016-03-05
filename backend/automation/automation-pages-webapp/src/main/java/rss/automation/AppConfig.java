package rss.automation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by michaeld on 05/03/2016.
 */
@Configuration
@ComponentScan(basePackages = "rss")
//@PropertySource("classpath:build.properties")
public class AppConfig {

//    @Bean
//    public static PropertySourcesPlaceholderConfigurer properties() {
//        return new PropertySourcesPlaceholderConfigurer();
//    }
}
