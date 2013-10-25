package rss.services.shows;

import com.google.common.base.Predicate;
import rss.controllers.vo.ShowsScheduleVO;
import rss.entities.Episode;
import rss.entities.Show;
import rss.entities.User;
import rss.services.matching.MatchCandidate;
import rss.services.requests.episodes.EpisodeRequest;
import rss.services.requests.episodes.ShowRequest;

import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 05/01/13 18:01
 */
public interface ShowService {

	void saveNewShow(Show show);

	void transformEpisodeRequest(ShowRequest episodeRequest);

	void downloadShowList();

	DownloadScheduleResult downloadLatestScheduleWithTorrents();

	void downloadFullScheduleWithTorrents(Show show, boolean torrentsDownloadAsync);

	DownloadScheduleResult downloadFullSchedule(final Show show);

	List<ShowAutoCompleteItem> autoCompleteShowNames(String term, boolean includeEnded, Predicate<? super ShowAutoCompleteItem> predicate);

	ShowsScheduleVO getSchedule(User user);

	List<MatchCandidate> filterMatching(EpisodeRequest mediaRequest, Collection<MatchCandidate> movieRequests);

	void persistEpisodeToShow(Show show, Episode episode);

	Collection<Episode> findMissingFullSeasonEpisodes(Show show);

	void disconnectTorrentsFromEpisode(Episode episode);

	void downloadEpisode(User user, long torrentId);
}
