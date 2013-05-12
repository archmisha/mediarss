package rss.services.downloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.dao.EpisodeDao;
import rss.dao.SubtitlesDao;
import rss.entities.Episode;
import rss.entities.Subtitles;
import rss.services.requests.SubtitlesRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SubCenterSubtitlesSearcher;
import rss.services.subtitles.SubtitleLanguage;
import rss.services.subtitles.SubtitlesTrackerService;

import java.util.*;

/**
 * User: dikmanm
 * Date: 11/05/13 15:21
 */
@Service
public class SubtitlesDownloader extends BaseDownloader<SubtitlesRequest, Subtitles> {

	@Autowired
	private SubtitlesDao subtitlesDao;

	@Autowired
	private SubtitlesTrackerService subtitlesTrackerService;

	@Autowired
	private SubCenterSubtitlesSearcher subCenterSubtitlesSearcher;

	@Autowired
	private EpisodeDao episodeDao;

	@Override
	protected void processMissingRequests(Collection<SubtitlesRequest> missing) {
		for (SubtitlesRequest subtitlesRequest : missing) {
			for (Episode episode : subtitlesRequest.getEpisodes()) {
				episode.setSubtitlesScanDate(new Date());
			}
		}
	}

	@Override
	protected Collection<Subtitles> preDownloadPhase(Set<SubtitlesRequest> requests, boolean forceDownload) {
		Set<Subtitles> result = new HashSet<>();
		for (SubtitlesRequest request : requests) {
			// todo: somewhere validate if need to download - print if skipping due to already scanned
			// todo: should keep scan date per langauge? maybe last time scanned for some other language for that torrent or episode
			// todo: why not keep scan date per torrent and not per episode?

			for (SubtitleLanguage subtitleLanguage : new ArrayList<>(request.getLanguages())) {
				Subtitles subtitles = subtitlesDao.find(request.getTorrent(), subtitleLanguage);
				if (subtitles != null) {
					result.add(subtitles);
					request.getLanguages().remove(subtitleLanguage);
				}
			}
			if (request.getLanguages().isEmpty()) {
				requests.remove(request);
			}
		}
		return result;
	}

	@Override
	protected boolean validateSearchResult(SubtitlesRequest mediaRequest, SearchResult searchResult) {
		return true;
	}

	@Override
	protected List<Subtitles> processSearchResults(Collection<Pair<SubtitlesRequest, SearchResult>> results) {
		List<Subtitles> res = new ArrayList<>();
		for (Pair<SubtitlesRequest, SearchResult> result : results) {
			SubtitlesRequest subtitlesRequest = result.getKey();
			SearchResult searchResult = result.getValue();


			for (Subtitles subtitles : searchResult.<Subtitles>getDownloadables()) {
				Subtitles persistedSubtitles = subtitlesDao.find(subtitlesRequest.getTorrent(), subtitles.getLanguage());
				if (persistedSubtitles == null) {
					subtitles.setTorrent(subtitlesRequest.getTorrent());
					subtitlesDao.persist(subtitles);
					subtitlesTrackerService.announce(subtitles);
				}
			}

			for (Episode episode : subtitlesRequest.getEpisodes()) {
				Episode persistedEpisode = episodeDao.find(episode.getId());
				persistedEpisode.setSubtitlesScanDate(new Date());

				// update subCenter url of the show, in the request episode it must not be null, otherwise couldn't find the results
				if (StringUtils.isBlank(persistedEpisode.getShow().getSubcenterUrl())) {
					persistedEpisode.getShow().setSubcenterUrl(episode.getShow().getSubcenterUrl());
				}
			}
			res.addAll(searchResult.<Subtitles>getDownloadables());
		}
		return res;
	}

	@Override
	protected SearchResult downloadTorrent(SubtitlesRequest request) {
		return subCenterSubtitlesSearcher.search(request);
	}
}
