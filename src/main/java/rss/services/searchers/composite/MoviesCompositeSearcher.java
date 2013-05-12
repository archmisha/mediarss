package rss.services.searchers.composite;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import rss.entities.Torrent;
import rss.services.requests.MovieRequest;
import rss.services.searchers.SearchResult;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 19:15
 */
@Service("moviesCompositeSearcher")
public class MoviesCompositeSearcher extends DefaultCompositeSearcher<MovieRequest> {

	@Override
	protected void onTorrentFound(CompositeSearcherData compositeSearcherData,
								  SearchResult searchResult,
								  String searcherName) {
		// case of no IMDB url
		if (getImdbId(searchResult) == null) {
			compositeSearcherData.getNoIMDBUrlSearchers().add(searcherName);
		}
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
