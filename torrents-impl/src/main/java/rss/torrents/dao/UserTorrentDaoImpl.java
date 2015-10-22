package rss.torrents.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.torrents.UserTorrent;
import rss.user.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class UserTorrentDaoImpl extends BaseDaoJPA<UserTorrent> implements UserTorrentDao {

    @Override
    public List<UserTorrent> findUserMoviesForUserFeed(Date dateUploaded, User user) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("dateUploaded", dateUploaded);
        params.put("userId", user.getId());
        return super.findByNamedQueryAndNamedParams("UserMovieTorrent.findUserMoviesForUserFeed", params);
    }

    @Override
    public List<UserTorrent> findEpisodesAddedSince(User user, Date dateAdded) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("dateAdded", dateAdded);
        params.put("userId", user.getId());
        return super.findByNamedQueryAndNamedParams("UserEpisodeTorrent.findEpisodesAddedSince", params);
    }

    @Override
    public List<UserTorrent> findUserEpisodeTorrentByTorrentId(long torrentId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("torrentId", torrentId);
        return super.findByNamedQueryAndNamedParams("UserEpisodeTorrent.findUserTorrentByTorrentId", params);
    }
}
