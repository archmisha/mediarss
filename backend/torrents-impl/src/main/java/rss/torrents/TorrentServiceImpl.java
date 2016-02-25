package rss.torrents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.torrents.dao.TorrentDao;

import java.util.Collection;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 18/10/2015 20:48
 */
@Component
public class TorrentServiceImpl implements TorrentService {

    @Autowired
    private TorrentDao torrentDao;

    @Override
    public Collection<Torrent> find(Set<Long> torrentIds) {
        return torrentDao.find(torrentIds);
    }

    @Override
    public Collection<Torrent> find(Collection<Long> torrentIds) {
        return torrentDao.find(torrentIds);
    }

    @Override
    public Torrent find(long torrentId) {
        return torrentDao.find(torrentId);
    }
}
