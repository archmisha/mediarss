package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.entities.MediaQuality;
import rss.entities.Torrent;
import rss.services.log.LogService;
import rss.services.requests.EpisodeRequest;
import rss.services.requests.ShowRequest;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 18:59
 */
@Service("smartEpisodeSearcher")
public class SmartEpisodeSearcher implements TorrentSearcher<ShowRequest, Episode> {

	@Autowired
	@Qualifier("compositeEpisodeSearcher")
	private TorrentSearcher<ShowRequest, Episode> compositeEpisodeSearcher;

	@Autowired
	private LogService logService;

	private List<EpisodeModificator> episodeModificators;

	@PostConstruct
	private void postConstruct() {
		episodeModificators = Arrays.asList(
				new DoNothingEpisodeModificator(),
				new NormalQualityEpisodeModificator(),
				new ShowNameEndsWithYearEpisodeModificator());
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public SearchResult search(ShowRequest episodeRequest) {
		String msgPrefix = null;
		for (EpisodeModificator episodeModificator : episodeModificators) {
			ShowRequest modifiedRequest = episodeModificator.modify(episodeRequest);
			if (modifiedRequest != null) {
				if (msgPrefix != null) { // skipping the first one
					logService.info(getClass(), String.format("%s %s (modificator: %s).",
							msgPrefix, episodeModificator.getDescription(), episodeModificator.getClass().getSimpleName()));
				}

				SearchResult searchResult = compositeEpisodeSearcher.search(episodeRequest);
				if (searchResult.getSearchStatus() != SearchResult.SearchStatus.NOT_FOUND) {
					// update the quality, cuz one of the modificators search with different qualities, so
					// maybe found with diff quality than originally requested
					for (Torrent torrent : searchResult.getTorrents()) {
						torrent.setQuality(modifiedRequest.getQuality());
					}

					return searchResult;
				}

				msgPrefix = "Episode \"" + modifiedRequest.toString() + "\" is not found.";
			}
		}
		return SearchResult.createNotFound();
	}

	private interface EpisodeModificator {
		ShowRequest modify(ShowRequest episodeRequest);

		String getDescription();
	}

	private class DoNothingEpisodeModificator implements EpisodeModificator {
		@Override
		public ShowRequest modify(ShowRequest episodeRequest) {
			return episodeRequest;
		}

		@Override
		public String getDescription() {
			return "";
		}
	}

	private class NormalQualityEpisodeModificator implements EpisodeModificator {
		@Override
		public ShowRequest modify(ShowRequest episodeRequest) {
			EpisodeRequest copy = episodeRequest.copy();
			copy.setQuality(MediaQuality.NORMAL);
			return copy;
		}

		@Override
		public String getDescription() {
			return "Trying to find with non HD quality.";
		}
	}

	private static class ShowNameEndsWithYearEpisodeModificator implements EpisodeModificator {

		private static Pattern p = Pattern.compile("(.+) \\(\\d+\\)");

		@Override
		public ShowRequest modify(ShowRequest episodeRequest) {
			Matcher matcher = p.matcher(episodeRequest.getTitle());
			if (matcher.find()) {
				EpisodeRequest copy = episodeRequest.copy();
				copy.setTitle(matcher.group(1));
				return copy;
			}
			return null;
		}

		@Override
		public String getDescription() {
			return "Trying without the year suffix.";
		}
	}
}
