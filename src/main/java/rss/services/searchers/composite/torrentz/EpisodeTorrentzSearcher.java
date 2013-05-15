package rss.services.searchers.composite.torrentz;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.MediaRSSException;
import rss.services.matching.MatchCandidate;
import rss.services.requests.episodes.ShowRequest;
import rss.services.matching.MatcherVisitor;
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
@Service
public class EpisodeTorrentzSearcher extends TorrentzSearcher<ShowRequest> {

	@Autowired
	private ShowService showService;

	@Override
	protected String getSearchUrl(ShowRequest mediaRequest) {
		try {
			return TorrentzParserImpl.TORRENTZ_EPISODE_SEARCH_URL + URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new MediaRSSException("Failed encoding "+ mediaRequest.toQueryString() + ": " + e.getMessage(), e);
		}
	}

	@Override
	protected void prepareSearchRequest(ShowRequest mediaRequest) {
		String url = getSearchUrl(mediaRequest);
		Set<TorrentzResult> torrentzResults = torrentzParser.downloadByUrl(url);

		List<MatchCandidate> filteredResults = filterMatching(mediaRequest, torrentzResults);
		if (filteredResults.isEmpty()) {
			return;
		}

		TorrentzResult bestResult = new Ordering<MatchCandidate>() {
			@Override
			public int compare(MatchCandidate movieRequest1, MatchCandidate movieRequest2) {
				return Ints.compare(movieRequest1.<TorrentzResult>getObject().getUploaders(), movieRequest2.<TorrentzResult>getObject().getUploaders());
			}
		}.max(filteredResults).getObject();

		mediaRequest.setHash(bestResult.getHash());
		mediaRequest.setUploaders(bestResult.getUploaders());

		// parse single search result torrent entry
		enrichRequestWithSearcherIds(mediaRequest);
	}

	protected List<MatchCandidate> filterMatching(ShowRequest mediaRequest, Set<TorrentzResult> mediaRequests) {
		List<MatchCandidate> matchCandidates = new ArrayList<>();
		for (final TorrentzResult curRequest : mediaRequests) {
			matchCandidates.add(new MatchCandidate() {
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
