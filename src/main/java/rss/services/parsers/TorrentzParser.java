package rss.services.parsers;

import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 22:08
 */
public interface TorrentzParser {

    <T> Set<T> parse(String page);

	String getPirateBayId(String page);

	String getKickAssTorrentsId(String page);
}
