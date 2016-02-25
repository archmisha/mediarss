package rss.scheduler;

/**
 * User: dikmanm
 * Date: 25/02/2015 09:30
 */
public interface ScheduledJob {

    void run();

    String getName();

    String getCronExp();
}
