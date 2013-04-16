package rss.services.shows;

import rss.controllers.vo.EpisodeSearchResult;
import rss.entities.User;
import rss.services.requests.ShowRequest;

/**
 * User: dikmanm
 * Date: 16/04/13 17:14
 */
public interface ShowSearchService {

	EpisodeSearchResult search(ShowRequest episodeRequest, User user);

}
