package rss.services.shows;

import com.google.common.base.Predicate;
import rss.controllers.vo.EpisodeSearchResult;
import rss.controllers.vo.ShowsScheduleVO;
import rss.entities.Episode;
import rss.entities.User;
import rss.services.requests.EpisodeRequest;
import rss.entities.Show;
import rss.services.requests.ShowRequest;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 05/01/13 18:01
 */
public interface ShowService {

	void transformEpisodeRequest(ShowRequest episodeRequest);

	void downloadShowList();

	DownloadScheduleResult downloadLatestScheduleWithTorrents();

	void downloadFullScheduleWithTorrents(Show show);

	DownloadScheduleResult downloadFullSchedule(final Show show);

	List<AutoCompleteItem> autoCompleteShowNames(String term, boolean includeEnded, Predicate<? super AutoCompleteItem> predicate);

	ShowsScheduleVO getSchedule(Set<Show> shows);

	boolean isMatch(EpisodeRequest mediaRequest, String title);

	void persistEpisodeToShow(Show show, Episode episode);

	Collection<Episode> findMissingFullSeasonEpisodes(Show show);
}
