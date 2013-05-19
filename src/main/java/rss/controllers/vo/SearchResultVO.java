package rss.controllers.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * User: dikmanm
 * Date: 21/02/13 22:25
 */
public class SearchResultVO {

	private Collection<UserTorrentVO> episodes;
	private String originalSearchTerm;
	private String actualSearchTerm;
	private Collection<ShowVO> didYouMean;

	public SearchResultVO(String originalSearchTerm, String actualSearchTerm, Collection<UserTorrentVO> episodes) {
		this.episodes = episodes;
		this.originalSearchTerm = originalSearchTerm;
		this.actualSearchTerm = actualSearchTerm;
		this.didYouMean = new ArrayList<>();
	}

	public Collection<UserTorrentVO> getEpisodes() {
		return episodes;
	}

	public String getOriginalSearchTerm() {
		return originalSearchTerm;
	}

	public String getActualSearchTerm() {
		return actualSearchTerm;
	}

	public Collection<ShowVO> getDidYouMean() {
		return didYouMean;
	}

	public static SearchResultVO createNoResults(String searchTerm) {
		return new SearchResultVO(searchTerm, searchTerm, Collections.<UserTorrentVO>emptyList());
	}

	public static SearchResultVO createDidYouMean(String searchTerm, Collection<ShowVO> shows) {
		SearchResultVO esr = new SearchResultVO(searchTerm, null, Collections.<UserTorrentVO>emptyList());
		esr.didYouMean.addAll(shows);
		return esr;
	}

	public static SearchResultVO createWithResult(String originalSearchTerm, String actualSearchTerm,
												  Collection<UserTorrentVO> episodes, Collection<ShowVO> shows) {
		SearchResultVO esr = new SearchResultVO(originalSearchTerm, actualSearchTerm, episodes);
		esr.didYouMean.addAll(shows);
		return esr;
	}
}
