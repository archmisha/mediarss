package rss.services.shows;

import rss.controllers.vo.SearchResultVO;
import rss.entities.Show;
import rss.entities.User;
import rss.services.requests.episodes.ShowRequest;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 16/04/13 17:14
 */
public interface ShowSearchService {

	SearchResultVO search(ShowRequest episodeRequest, User user, boolean forceDownload);

	Collection<CachedShow> statisticMatch(String name);
}
