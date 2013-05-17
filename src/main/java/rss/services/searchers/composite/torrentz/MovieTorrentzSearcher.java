package rss.services.searchers.composite.torrentz;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import rss.MediaRSSException;
import rss.entities.Torrent;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.SearchResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

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
	public SearchResult search(MovieRequest mediaRequest) {
		String url = getSearchUrl(mediaRequest);
		Set<TorrentzResult> torrentzResults = torrentzParser.downloadByUrl(url);

		// group requests by name and for each name leave only the one with the best seeders
		// this way we eliminate torrents with same name and lower seeders number
		Map<String, TorrentzResult> map = new HashMap<>();
		for (TorrentzResult foundRequest : torrentzResults) {
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

			SearchResult searchResult = super.search(curRequest);

			// null means not found
			if (searchResult != null) {
				// override torrent titles, because it might differ from the torrentz side, which combines all
				// other sites. And the we get double torrent names
				for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
					torrent.setTitle(foundRequest.getTitle());
				}

				if (searchResult.getSearchStatus() == SearchResult.SearchStatus.FOUND) {
					results.add(searchResult);
				} else if (searchResult.getSearchStatus() == SearchResult.SearchStatus.AWAITING_AGING) {
					awaitingAgingResult = searchResult;
				}
			}
		}

		if (!results.isEmpty()) {
			// merge results into one
			SearchResult searchResult = results.remove(0);
			for (SearchResult result : results) {
				searchResult.getDownloadables().addAll(result.getDownloadables());
				searchResult.appendSource(result.getSource());
			}

			return searchResult;
		}

		if (awaitingAgingResult != null) {
			return awaitingAgingResult;
		}

		return SearchResult.createNotFound();
	}

	@Override
	protected String onTorrentFound(SearchResult searchResult) {
		// case of no IMDB url
		if (getImdbId(searchResult) == null) {
			return "no IMDB id";
		}
		return null;
	}

	private String getImdbId(SearchResult searchResult) {
		for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
			if (!StringUtils.isBlank(torrent.getImdbId())) {
				return torrent.getImdbId();
			}
		}
		return null;
	}
}
