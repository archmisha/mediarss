package rss.services.shows;

import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 17:39
 */
public interface ShowRssService {

//	Set<EpisodeRequest> getTVShowsEpisodes(String username, String password);

	boolean validateCredentials(String username, String password);

	Collection<String> getShows(String username, String password);
}
