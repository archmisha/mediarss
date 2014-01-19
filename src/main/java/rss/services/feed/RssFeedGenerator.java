package rss.services.feed;

import rss.entities.Torrent;
import rss.entities.User;

import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 25/11/12
 * Time: 17:26
 */
public interface RssFeedGenerator {

	String generateFeed(User user);

	Set<Torrent> getFeedTorrents(User user);
}
