package rss.services.shows;

import rss.controllers.vo.SearchResultVO;
import rss.entities.Episode;
import rss.services.downloader.DownloadResult;
import rss.services.requests.episodes.ShowRequest;

/**
 * User: dikmanm
 * Date: 06/11/13 23:32
 */
public class UserActiveSearch {

	private SearchResultVO searchResultVO;
	private DownloadResult<Episode, ShowRequest> downloadResult;

	public UserActiveSearch(SearchResultVO searchResultVO, DownloadResult<Episode, ShowRequest> downloadResult) {
		this.searchResultVO = searchResultVO;
		this.downloadResult = downloadResult;
	}

	public SearchResultVO getSearchResultVO() {
		return searchResultVO;
	}

	public DownloadResult<Episode, ShowRequest> getDownloadResult() {
		return downloadResult;
	}
}
