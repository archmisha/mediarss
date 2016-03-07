package rss.deployer;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by michaeld on 05/03/2016.
 */
@Configuration
@ComponentScan(basePackages = "rss")
@PropertySources({
        @PropertySource("file:${lookup.dir}/database.properties"),
        @PropertySource("file:${lookup.dir}/mongodb.properties"),
})
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
