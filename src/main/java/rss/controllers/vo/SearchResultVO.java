package rss.controllers.vo;

import java.util.*;

/**
 * User: dikmanm
 * Date: 21/02/13 22:25
 */
public class SearchResultVO {

	private String id;
	private Collection<UserTorrentVO> episodes;
	private Date start;
	private Date end;
	private String originalSearchTerm;
	private String actualSearchTerm;
	private String displayLabel;
	private Collection<ShowVO> didYouMean;

	public SearchResultVO(String originalSearchTerm, String actualSearchTerm, Collection<UserTorrentVO> episodes) {
		this.episodes = episodes;
		this.originalSearchTerm = originalSearchTerm;
		this.actualSearchTerm = actualSearchTerm;
		this.didYouMean = new ArrayList<>();
		this.start = new Date();
		this.end = new Date();
		this.id = UUID.randomUUID().toString();
	}

	public SearchResultVO(String originalSearchTerm, String actualSearchTerm) {
		this(originalSearchTerm, actualSearchTerm, new ArrayList<UserTorrentVO>());
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

	public Date getStart() {
		return start;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Date getEnd() {
		return end;
	}

	public String getId() {
		return id;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public void setEpisodes(Collection<UserTorrentVO> episodes) {
		this.episodes = episodes;
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
												  Collection<UserTorrentVO> results, Collection<ShowVO> shows) {
		SearchResultVO esr = new SearchResultVO(originalSearchTerm, actualSearchTerm, results);
		esr.didYouMean.addAll(shows);
		return esr;
	}

	public static SearchResultVO createWithResult(String originalSearchTerm, String actualSearchTerm, String displayLabel,
												  Collection<ShowVO> shows) {
		SearchResultVO esr = new SearchResultVO(originalSearchTerm, actualSearchTerm);
		esr.setDisplayLabel(displayLabel);
		esr.didYouMean.addAll(shows);
		esr.setEnd(null); // in progress
		return esr;
	}
}
