package rss.torrents.searchers;

import org.apache.commons.lang3.StringUtils;
import rss.torrents.Downloadable;

import java.util.*;

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

	public enum SearcherFailedReason {
		NOT_FOUND {
			public String toString() {
				return "not found";
			}
		},
		NO_IMDB_ID {
			public String toString() {
				return "no IMDB ID";
			}
		},
		EXCEPTION {
			public String toString() {
				return "exception";
			}
		},
		NO_SUBCENTER_URL {
			public String toString() {
				return "no subcenter url";
			}
		}
	}

	private SearchStatus searchStatus;
	private List<Downloadable> downloadables;
	private Set<String> sources;
	private Map<String, SearcherFailedReason> failedSearchers;

	private SearchResult(SearchStatus searchStatus) {
		this.downloadables = new ArrayList<>();
		this.failedSearchers = new HashMap<>();
		this.sources = new HashSet<>();
		this.searchStatus = searchStatus;
	}

	public SearchResult(String source) {
		this(SearchStatus.FOUND);
		this.sources.add(source);
	}

	public static SearchResult createNotFound() {
		return new SearchResult(SearchStatus.NOT_FOUND);
	}

	public static SearchResult createNotFound(Map<String, SearcherFailedReason> failedSearchers) {
		SearchResult searchResult = createNotFound();
		searchResult.addFailedSearchers(failedSearchers);
		return searchResult;
	}

	public static SearchResult createNotFound(String searcherName, SearcherFailedReason failedReason) {
		SearchResult searchResult = createNotFound();
		searchResult.addFailedSearchers(Collections.singletonMap(searcherName, failedReason));
		return searchResult;
	}

	public void addFailedSearchers(Map<String, SearcherFailedReason> failedSearchers) {
		// smart overriding
		for (Map.Entry<String, SearcherFailedReason> entry : failedSearchers.entrySet()) {
			if (!this.failedSearchers.containsKey(entry.getKey()) && !sources.contains(entry.getKey())) {
				this.failedSearchers.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public Map<String, SearcherFailedReason> getFailedSearchers() {
		return new HashMap<>(failedSearchers);
	}

	public String getFailedSearchersDisplayString() {
		StringBuilder sb = new StringBuilder();
		if (!failedSearchers.isEmpty()) {
			for (Map.Entry<String, SearcherFailedReason> entry : failedSearchers.entrySet()) {
				sb.append(entry.getKey()).append(" - ").append(entry.getValue().toString()).append(", ");
			}
			sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
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

	public String getSourcesDisplayString() {
		return StringUtils.join(sources, ", ");
	}

	public void addSources(Collection<String> sources) {
		this.sources.addAll(sources);
		for (String source : sources) {
			failedSearchers.remove(source);
		}
	}

	public Collection<String> getSources() {
		return Collections.unmodifiableCollection(sources);
	}

	public String getDownloadablesDisplayString() {
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