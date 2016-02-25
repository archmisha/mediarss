package rss.shows;

import com.google.common.base.Predicate;
import rss.shows.schedule.ShowsScheduleJSON;
import rss.torrents.Episode;
import rss.torrents.Show;
import rss.torrents.matching.MatchCandidate;
import rss.torrents.requests.shows.EpisodeRequest;
import rss.torrents.requests.shows.ShowRequest;
import rss.user.User;

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

    List<ShowAutoCompleteItem> autoCompleteShowNames(String term, boolean includeEnded, Predicate<? super ShowAutoCompleteItem> predicate);

    ShowsScheduleJSON getSchedule(User user);

    List<MatchCandidate> filterMatching(EpisodeRequest mediaRequest, Collection<MatchCandidate> movieRequests);

    void persistEpisodeToShow(Show show, Episode episode);

    Collection<Episode> findMissingFullSeasonEpisodes(Show show);

    void disconnectTorrentsFromEpisode(Episode episode);

    void downloadEpisode(User user, long torrentId);

    List<Show> getTrackedShows(User user);

    Show find(long showId);

    Collection<CachedShow> findCachedShows();

    List<Episode> find(ShowRequest showRequest);

    void delete(Episode episode);

    void delete(Show show);

    boolean isShowBeingTracked(Show show);

    Collection<Episode> getEpisodesToDownload(User user);

    Show findByName(String name);

    Show findByTvRageId(int tvRageId);

    void updateShow(Show show);

    void downloadSchedule(Show show);

    void addTrackedShow(User user, long showId);
}
