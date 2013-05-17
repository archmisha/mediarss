package rss.services.searchers;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 26/11/12
 * Time: 00:46
 */
public class SearchResult {

	// awaiting aging is when torrent was found but still the aging period hasn't passed
	public enum SearchStatus {
		NOT_FOUND, FOUND, AWAITING_AGING
	}

	private SearchStatus searchStatus;
	private List<Downloadable> downloadables;
	private String source;
	private List<Pair<String, String>> failedSearchers;

	public SearchResult(SearchStatus searchStatus) {
		downloadables = new ArrayList<>();
		failedSearchers = new ArrayList<>();
		this.searchStatus = searchStatus;
	}

	public SearchResult(String source) {
		this(SearchStatus.FOUND);
		this.source = source;
	}

	public static SearchResult createNotFound() {
		return new SearchResult(SearchStatus.NOT_FOUND);
	}

	public static SearchResult createNotFound(List<Pair<String, String>> failedSearchers) {
		SearchResult searchResult = createNotFound();
		searchResult.getFailedSearchers().addAll(failedSearchers);
		return searchResult;
	}

	// for tests
	public SearchResult(Downloadable downloadable, String source, SearchStatus searchStatus) {
		this(source);
		this.setSearchStatus(searchStatus);
		this.addDownloadable(downloadable);
	}

	public List<Pair<String, String>> getFailedSearchers() {
		return failedSearchers;
	}

	public SearchStatus getSearchStatus() {
		return searchStatus;
	}

	public void setSearchStatus(SearchStatus searchStatus) {
		this.searchStatus = searchStatus;
	}

	@SuppressWarnings("unchecked")
	public <T extends Downloadable> List<T> getDownloadables() {
		return (List<T>) downloadables;
	}

	public void addDownloadable(Downloadable downloadable) {
		this.downloadables.add(downloadable);
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public void appendSource(String source) {
		if (this.source == null) {
			this.source = source;
		} else if (!this.source.contains(source)) {
			this.source += ", " + source;
		}
	}

	public String getTorrentTitles() {
		StringBuilder sb = new StringBuilder();
		for (Downloadable downloadable : downloadables) {
			sb.append(downloadable.getName());
			sb.append(", ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
