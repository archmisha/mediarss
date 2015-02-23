package rss.environment;

/**
 * User: dikmanm
 * Date: 21/02/2015 20:24
 */
public class EnvironmentTestUtils {

    public static void inject(Environment env) {
        Environment.setInstance(env);
    }
}
