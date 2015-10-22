package rss.shows;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.shows.dao.UserEpisodeTorrentDao;
import rss.shows.dao.UserEpisodeTorrentImpl;
import rss.torrents.Episode;
import rss.torrents.Subtitles;
import rss.torrents.UserTorrent;
import rss.user.User;

import java.util.*;

/**
 * User: dikmanm
 * Date: 16/10/2015 23:05
 */
@Repository
public class UserEpisodeTorrentDaoImpl extends BaseDaoJPA<UserEpisodeTorrent> implements UserEpisodeTorrentDao {

    @Override
    protected Class<? extends UserEpisodeTorrent> getPersistentClass() {
        return UserEpisodeTorrentImpl.class;
    }

    @Override
    public UserEpisodeTorrent findUserEpisodeTorrent(User user, long torrentId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", user.getId());
        params.put("torrentIds", Collections.singleton(torrentId));
        List<UserEpisodeTorrent> list = super.findByNamedQueryAndNamedParams("UserEpisodeTorrent.findUserEpisodeTorrents", params);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Collection<UserTorrent> findUserEpisodes(long userId, Collection<Episode> episodes) {
        Set<Long> ids = new HashSet<>();
        for (Episode episode : episodes) {
            ids.addAll(episode.getTorrentIds());
        }

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        params.put("torrentIds", ids);
        return super.findByNamedQueryAndNamedParams("UserEpisodeTorrent.findUserEpisodeTorrents", params);
    }
}
