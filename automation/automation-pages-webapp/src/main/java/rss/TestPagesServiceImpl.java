package rss;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import rss.shows.thetvdb.TheTvDbConstants;
import rss.shows.thetvdb.TheTvDbEpisode;
import rss.shows.thetvdb.TheTvDbShow;

import java.util.*;

/**
 * User: dikmanm
 * Date: 17/08/2015 10:05
 */
@Component
public class TestPagesServiceImpl {

    private Map<Long, ShowWrapper> shows = new HashMap<>();
    private Map<Long, EpisodeWrapper> episodes = new HashMap<>();
    private Set<Torrent> torrentIds = new HashSet<>();

    public void resetOverrides() {
        shows = new HashMap<>();
        episodes = new HashMap<>();
        torrentIds = new HashSet<>();
    }

    public boolean hasTorrentId(String torrentId) {
        for (Torrent torrent : torrentIds) {
            StringBuilder sb = new StringBuilder();
            sb.append("s").append(StringUtils.leftPad(torrent.getSeason(), 2, '0'))
                    .append("e").append(StringUtils.leftPad(torrent.getEpisode(), 2, '0'));
            if (torrentId.contains(torrent.getShowName()) && torrentId.contains(sb.toString())) {
                return true;
            }
        }
        return false;
    }

    public void markShowEnded(long theTvDbShowId) {
        ShowWrapper show = shows.get(theTvDbShowId);
        if (show == null) {
            throw new RuntimeException("Show " + theTvDbShowId + " not found");
        }
        show.getShow().setStatus(TheTvDbConstants.ENDED_STATUS);
        show.setUpdateTime(System.currentTimeMillis());
    }

    public Collection<TheTvDbShow> getAllShows() {
        return Collections2.transform(shows.values(), new Function<ShowWrapper, TheTvDbShow>() {
            @Override
            public TheTvDbShow apply(ShowWrapper showWrapper) {
                return showWrapper.getShow();
            }
        });
    }

    public TheTvDbShow getShow(String name) {
        for (ShowWrapper show : shows.values()) {
            if (show.getShow().getName().equals(name)) {
                return show.getShow();
            }
        }
        return null;
    }

    public TheTvDbShow getShow(long theTvDbShowId) {
        return shows.get(theTvDbShowId).getShow();
    }

    public List<TheTvDbEpisode> getEpisodes(long serverTime) {
        List<TheTvDbEpisode> result = new ArrayList<>();
        for (EpisodeWrapper episode : this.episodes.values()) {
            if (episode.getUpdateTime() >= serverTime) {
                result.add(episode.getEpisode());
            }
        }
        return result;
    }

    public List<TheTvDbShow> getShows(long serverTime) {
        List<TheTvDbShow> result = new ArrayList<>();
        for (ShowWrapper show : this.shows.values()) {
            if (show.getUpdateTime() >= serverTime) {
                result.add(show.getShow());
            }
        }
        return result;
    }

    public List<TheTvDbEpisode> getEpisodesByShow(long theTvDbShowId) {
        List<TheTvDbEpisode> result = new ArrayList<>();
        for (EpisodeWrapper episode : this.episodes.values()) {
            if (episode.getEpisode().getShowId() == theTvDbShowId) {
                result.add(episode.getEpisode());
            }
        }
        return result;
    }

    public void createShow(TheTvDbShow show) {
        shows.put(show.getId(), new ShowWrapper(show, System.currentTimeMillis()));
    }

    public void createEpisode(TheTvDbEpisode episode) {
        episodes.put(episode.getId(), new EpisodeWrapper(episode, System.currentTimeMillis()));
    }

    public TheTvDbEpisode getEpisode(Long theTvDbEpisodeId) {
        return episodes.get(theTvDbEpisodeId).getEpisode();
    }

    public void createTorrent(String showName, String season, String episode) {
        torrentIds.add(new Torrent(showName, season, episode));
    }
}
