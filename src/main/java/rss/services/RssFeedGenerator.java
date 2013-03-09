package rss.services;

import rss.entities.User;

/**
 * User: Michael Dikman
 * Date: 25/11/12
 * Time: 17:26
 */
public interface RssFeedGenerator {

	String generateFeed(User user);
}
