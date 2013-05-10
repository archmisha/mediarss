package rss.services.searchers.composite.torrentz;

import org.springframework.stereotype.Service;
import rss.entities.Movie;
import rss.entities.Torrent;
import rss.services.requests.MovieRequest;
import rss.services.searchers.SearchResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * User: dikmanm
 * Date: 30/04/13 22:35
 */
@Service("movieTorrentzSearcher")
public class MovieTorrentzSearcher extends TorrentzSearcher<MovieRequest, Movie> {

	@Override
	protected String getSearchUrl(MovieRequest mediaRequest) throws UnsupportedEncodingException {
		return TorrentzParserImpl.TORRENTZ_MOVIE_SEARCH_URL + URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8");
	}

	@Override
	protected boolean shouldFailOnNoIMDBUrl() {
		return true;
	}

	@Override
	protected SearchResult processTorrentzResults(MovieRequest originalRequest, Set<TorrentzResult> foundRequests) {
		// group requests by name and for each name leave only the one with the best seeders
		// this way we eliminate torrents with same name and lower seeders number
		Map<String, TorrentzResult> map = new HashMap<>();
		for (TorrentzResult foundRequest : foundRequests) {
			String title = foundRequest.getTitle();
			if (map.containsKey(title)) {
				if (map.get(title).getUploaders() < foundRequest.getUploaders()) {
					map.put(title, foundRequest);
				}
			} else {
				map.put(title, foundRequest);
			}
		}

		List<SearchResult> results = new ArrayList<>();
		SearchResult awaitingAgingResult = null;
		for (TorrentzResult foundRequest : map.values()) {
			MovieRequest curRequest = new MovieRequest(foundRequest.getTitle(), foundRequest.getHash());
			curRequest.setUploaders(foundRequest.getUploaders());
			enrichRequestWithSearcherIds(curRequest);

			CompositeSearcherData compositeSearcherData = new CompositeSearcherData();
			super.searchHelper(curRequest, compositeSearcherData);
			SearchResult searchResult = compositeSearcherData.getSuccessfulSearchResult();

			// null means not found
			if (searchResult != null) {
				// override torrent titles, because it might differ from the torrentz side, which combines all
				// other sites. And the we get double torrent names
				for (Torrent torrent : searchResult.getTorrents()) {
					torrent.setTitle(foundRequest.getTitle());
				}

				if (searchResult.getSearchStatus() == SearchResult.SearchStatus.FOUND) {
					results.add(searchResult);
				} else if (searchResult.getSearchStatus() == SearchResult.SearchStatus.AWAITING_AGING) {
					awaitingAgingResult = searchResult;
				}
			}
		}

		if (results.isEmpty()) {
			if (awaitingAgingResult != null) {
				return awaitingAgingResult;
			} else {
				return SearchResult.createNotFound();
			}
		}

		// merge results into one
		SearchResult searchResult = results.remove(0);
		searchResult.setSource(this.getName());
		for (SearchResult result : results) {
			searchResult.getTorrents().addAll(result.getTorrents());
		}

		return searchResult;
	}
}
