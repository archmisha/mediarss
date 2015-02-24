package rss.scheduler;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * User: Michael Dikman
 * Date: 09/12/12
 * Time: 20:54
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Scope("prototype")
public @interface QuartzJob {
    String name();

    String group() default "DEFAULT_GROUP";

    String cronExp();
}
