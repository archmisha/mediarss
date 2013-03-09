package rss.controllers.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: dikmanm
 * Date: 21/02/13 22:25
 */
public class EpisodeSearchResult {

	private Collection<UserTorrentVO> episodes;
	private String originalSearchTerm;
	private String actualSearchTerm;
	private Collection<ShowVO> didYouMean;

	public EpisodeSearchResult(String originalSearchTerm, String actualSearchTerm, Collection<UserTorrentVO> episodes) {
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

	public static EpisodeSearchResult createNoResults(String searchTerm) {
		return new EpisodeSearchResult(searchTerm, searchTerm, Collections.<UserTorrentVO>emptyList());
	}

	public static EpisodeSearchResult createDidYouMean(String searchTerm, Collection<ShowVO> shows) {
		EpisodeSearchResult esr = new EpisodeSearchResult(searchTerm, searchTerm, Collections.<UserTorrentVO>emptyList());
		esr.didYouMean.addAll(shows);
		return esr;
	}

	public void setDidYouMean(List<ShowVO> didYouMean) {
		this.didYouMean = didYouMean;
	}
}
