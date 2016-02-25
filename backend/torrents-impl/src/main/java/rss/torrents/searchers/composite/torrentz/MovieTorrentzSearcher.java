package rss.torrents.searchers.composite.torrentz;

import org.springframework.stereotype.Service;
import rss.MediaRSSException;
import rss.torrents.Torrent;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.searchers.SearchResult;
import rss.torrents.searchers.TorrentzResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 30/04/13 22:35
 */
@Service
public class MovieTorrentzSearcher extends TorrentzSearcher<MovieRequest> {

	@Override
	protected String getSearchUrl(MovieRequest mediaRequest) {
		try {
			return TorrentzParserImpl.TORRENTZ_MOVIE_SEARCH_URL + URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new MediaRSSException("Failed encoding " + mediaRequest.toQueryString() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public SearchResult search(MovieRequest movieRequest) {
		// optimization in case there is already hash given
		if (movieRequest.getHash() != null) {
			SearchResult searchResult = searchByHash(movieRequest);
			postProcessSearchResult(movieRequest, searchResult);
			return searchResult;
		}

		String url = getSearchUrl(movieRequest);
		Collection<TorrentzResult> torrentzResults = torrentzParser.downloadByUrl(url);

		List<SearchResult> foundSearchResults = new ArrayList<>();
		List<SearchResult> notFoundSearchResults = new ArrayList<>();
		SearchResult awaitingAgingResult = null;
		for (TorrentzResult foundRequest : torrentzResults) {
			MovieRequest curRequest = new MovieRequest(foundRequest.getTitle(), foundRequest.getHash());
//			curRequest.setUploaders(foundRequest.getUploaders());
			curRequest.setSize(foundRequest.getSize());
			enrichRequestWithSearcherIds(curRequest);

			SearchResult searchResult = super.search(curRequest);

			if (searchResult.getSearchStatus() == SearchResult.SearchStatus.NOT_FOUND) {
				notFoundSearchResults.add(searchResult);
			} else {
				postProcessSearchResult(curRequest, searchResult);

				if (searchResult.getSearchStatus() == SearchResult.SearchStatus.FOUND) {
					foundSearchResults.add(searchResult);
				} else if (searchResult.getSearchStatus() == SearchResult.SearchStatus.AWAITING_AGING) {
					awaitingAgingResult = searchResult;
				}
			}
		}

		if (!foundSearchResults.isEmpty()) {
			// merge results into one
			SearchResult searchResult = foundSearchResults.remove(0);
			for (SearchResult result : foundSearchResults) {
				searchResult.getDownloadables().addAll(result.getDownloadables());
				searchResult.addSources(result.getSources());
				searchResult.addFailedSearchers(result.getFailedSearchers());
			}

			return searchResult;
		}

		if (awaitingAgingResult != null) {
			return awaitingAgingResult;
		}

		SearchResult notFound = SearchResult.createNotFound();
		for (SearchResult searchResult : notFoundSearchResults) {
			notFound.addFailedSearchers(searchResult.getFailedSearchers());
		}
		return notFound;
	}

	// override torrent titles, because it might differ from the torrentz side, which combines all
	// other sites. And the we get double torrent names
	private void postProcessSearchResult(MovieRequest curRequest, SearchResult searchResult) {
		for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
			torrent.setTitle(curRequest.getTitle());
			torrent.setSize(curRequest.getSize());
		}
	}

	private SearchResult searchByHash(MovieRequest movieRequest) {
		enrichRequestWithSearcherIds(movieRequest);
		return super.search(movieRequest);
	}

	@Override
	protected SearchResult.SearcherFailedReason onTorrentFound(SearchResult searchResult) {
		// case of no IMDB url
		if (getImdbId(searchResult) == null) {
			return SearchResult.SearcherFailedReason.NO_IMDB_ID;
		}
		return null;
	}

	private String getImdbId(SearchResult searchResult) {
		for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
			if (torrent.getImdbId() != null) {
				return torrent.getImdbId();
			}
		}
		return null;
	}
}
