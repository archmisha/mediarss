package rss.content;

import rss.environment.ServerMode;

import java.util.Set;

/**
 * User: dikmanm
 * Date: 16/08/2015 23:36
 */
public interface ContentLoader {

    Set<ServerMode> getSupportedModes();

    void load();
}
