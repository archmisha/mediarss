package rss.services.shows;

import rss.controllers.vo.EpisodeSearchResult;
import rss.entities.Show;
import rss.entities.User;
import rss.services.requests.episodes.ShowRequest;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 16/04/13 17:14
 */
public interface ShowSearchService {

	EpisodeSearchResult search(ShowRequest episodeRequest, User user, boolean forceDownload);

	Collection<Show> statisticMatch(String name);

}
