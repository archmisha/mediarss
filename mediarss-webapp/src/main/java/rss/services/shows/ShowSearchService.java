package rss.services.shows;

import rss.controllers.vo.SearchResultVO;
import rss.entities.Episode;
import rss.services.downloader.DownloadResult;
import rss.services.requests.episodes.ShowRequest;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 16/04/13 17:14
 */
public interface ShowSearchService {

	SearchResultVO search(ShowRequest episodeRequest, long userId, boolean forceDownload);

	Collection<CachedShow> statisticMatch(String name);

	void downloadResultToSearchResultVO(long userId, DownloadResult<Episode, ShowRequest> downloadResult, SearchResultVO searchResultVO);
}
