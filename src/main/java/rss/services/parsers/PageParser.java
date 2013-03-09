package rss.services.parsers;

import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 22:08
 */
public interface PageParser {

    <T> Set<T> parse(String page);
}
