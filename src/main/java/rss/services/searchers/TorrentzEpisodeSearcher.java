package rss.services.searchers;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.MediaRSSException;
import rss.entities.Episode;
import rss.services.requests.EpisodeRequest;
import rss.services.requests.FullSeasonRequest;
import rss.services.PageDownloader;
import rss.services.SearchResult;
import rss.services.downloader.MovieRequest;
import rss.services.log.LogService;
import rss.services.movies.TorrentzServiceImpl;
import rss.services.parsers.TorrentzParser;
import rss.services.shows.ShowService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 13/04/13 11:35
 */
@Service("torrentzEpisodeSearcher")
public class TorrentzEpisodeSearcher implements TorrentSearcher<EpisodeRequest, Episode> {

	private static final String NAME = TorrentzServiceImpl.HOST_NAME;
	private static final String TORRENTZ_EPISODE_SEARCH_URL = TorrentzServiceImpl.HOST_NAME + "verifiedP?f=";

	@Autowired
	protected LogService logService;

	@Autowired
	@Qualifier("thePirateBayEpisodeTorrentSearcher")
	private TorrentSearcher<EpisodeRequest, Episode> thePirateBayEpisodeSearcher;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	@Qualifier("torrentzParser")
	private TorrentzParser torrentzParser;

	@Autowired
	private ShowService showService;

	@Override
	public SearchResult<Episode> search(EpisodeRequest episodeRequest) {
		try {
			// search torrentz to get piratebay id and the use regular piratebay searcher
			String page = pageDownloader.downloadPage(TORRENTZ_EPISODE_SEARCH_URL + URLEncoder.encode(episodeRequest.toQueryString(), "UTF-8"));
			Set<MovieRequest> movieRequests = torrentzParser.parse(page);

			movieRequests = filterMatching(episodeRequest, movieRequests);

			if (movieRequests.isEmpty()) {
				return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
			}

			MovieRequest bestRequest = new Ordering<MovieRequest>() {
				@Override
				public int compare(MovieRequest movieRequest1, MovieRequest movieRequest2) {
					return Ints.compare(movieRequest1.getUploaders(), movieRequest2.getUploaders());
				}
			}.max(movieRequests);

			String entryPage = pageDownloader.downloadPage(TorrentzServiceImpl.TORRENTZ_ENTRY_URL + bestRequest.getHash());
			episodeRequest.setPirateBayId(torrentzParser.getPirateBayId(entryPage));
			episodeRequest.setHash(bestRequest.getHash());

			return thePirateBayEpisodeSearcher.search(episodeRequest);
		} catch (UnsupportedEncodingException e) {
			throw new MediaRSSException(e.getMessage(), e);
		}
	}

	private Set<MovieRequest> filterMatching(EpisodeRequest episodeRequest, Set<MovieRequest> movieRequests) {
		List<ShowService.MatchCandidate> matchCandidates = new ArrayList<>();
		for (final MovieRequest movieRequest : movieRequests) {
			matchCandidates.add(new ShowService.MatchCandidate() {
				@Override
				public String getText() {
					return movieRequest.getTitle();
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> T getObject() {
					return (T) movieRequest;
				}
			});
		}

		Set<MovieRequest> results = new HashSet<>();
		for (ShowService.MatchCandidate matchCandidate : showService.filterMatching(episodeRequest, matchCandidates)) {
			results.add(matchCandidate.<MovieRequest>getObject());
		}
		return results;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
