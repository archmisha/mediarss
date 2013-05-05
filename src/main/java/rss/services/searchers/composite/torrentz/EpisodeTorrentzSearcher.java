package rss.services.searchers.composite.torrentz;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.services.requests.MovieRequest;
import rss.services.requests.ShowRequest;
import rss.services.searchers.MatcherVisitor;
import rss.services.searchers.SearchResult;
import rss.services.shows.ShowService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 13/04/13 11:35
 */
@Service("torrentzEpisodeSearcher")
public class EpisodeTorrentzSearcher extends TorrentzSearcher<ShowRequest, Episode> {

	@Autowired
	private ShowService showService;

	@Override
	protected String getSearchUrl(ShowRequest mediaRequest) throws UnsupportedEncodingException {
		return TorrentzParserImpl.TORRENTZ_EPISODE_SEARCH_URL + URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8");
	}

	@Override
	protected boolean shouldFailOnNoIMDBUrl() {
		return false;
	}

	protected SearchResult processTorrentzResults(ShowRequest originalRequest, Set<TorrentzResult> foundRequests) {
		List<ShowService.MatchCandidate> filteredResults = filterMatching(originalRequest, foundRequests);
		if (filteredResults.isEmpty()) {
			return SearchResult.createNotFound();
		}

		TorrentzResult bestResult = new Ordering<ShowService.MatchCandidate>() {
			@Override
			public int compare(ShowService.MatchCandidate movieRequest1, ShowService.MatchCandidate movieRequest2) {
				return Ints.compare(movieRequest1.<TorrentzResult>getObject().getUploaders(), movieRequest2.<TorrentzResult>getObject().getUploaders());
			}
		}.max(filteredResults).getObject();

		originalRequest.setHash(bestResult.getHash());
		originalRequest.setUploaders(bestResult.getUploaders());

		// parse single search result torrent entry
		enrichRequestWithSearcherIds(originalRequest);

		CompositeSearcherData compositeSearcherData = new CompositeSearcherData();
		super.searchHelper(originalRequest, compositeSearcherData);
		SearchResult searchResult = compositeSearcherData.getSuccessfulSearchResult();
		// null means not found
		if (searchResult != null) {
			return searchResult;
		}
		return SearchResult.createNotFound();
	}

	protected List<ShowService.MatchCandidate> filterMatching(ShowRequest mediaRequest, Set<TorrentzResult> mediaRequests) {
		List<ShowService.MatchCandidate> matchCandidates = new ArrayList<>();
		for (final TorrentzResult curRequest : mediaRequests) {
			matchCandidates.add(new ShowService.MatchCandidate() {
				@Override
				public String getText() {
					return curRequest.getTitle();
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> T getObject() {
					return (T) curRequest;
				}
			});
		}

		return mediaRequest.visit(new MatcherVisitor(showService), matchCandidates);
	}
}
