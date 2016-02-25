package rss.movies.dao;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.movies.UserMovieTorrent;
import rss.torrents.Movie;
import rss.user.User;

import java.util.*;

/**
 * User: dikmanm
 * Date: 16/10/2015 23:03
 */
@Repository
public class UserMovieTorrentDaoImpl extends BaseDaoJPA<UserMovieTorrent> implements UserMovieTorrentDao {

    @Override
    protected Class<? extends UserMovieTorrent> getPersistentClass() {
        return UserMovieTorrentImpl.class;
    }

    @Override
    public List<UserMovieTorrent> findUserMovieTorrents(User user, Collection<Movie> movies) {
        if (movies.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", user.getId());
        params.put("movieIds", Collections2.transform(movies, new Function<Movie, Long>() {
            @Override
            public Long apply(Movie movie) {
                return movie.getId();
            }
        }));
        return super.findByNamedQueryAndNamedParams("UserMovieTorrent.findUserMovieTorrents", params);
    }

    @Override
    public UserMovieTorrent findUserMovieTorrent(User user, long torrentId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", user.getId());
        params.put("torrentIds", Collections.singleton(torrentId));
        List<UserMovieTorrent> list = super.findByNamedQueryAndNamedParams("UserMovieTorrent.findUserMovieTorrentsByTorrentIds", params);
        return list.isEmpty() ? null : list.get(0);
    }
}
