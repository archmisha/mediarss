package rss.torrents;

import java.util.Collection;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 18/10/2015 20:48
 */
public interface TorrentService {
    Collection<Torrent> find(Set<Long> torrentIds);

    Collection<Torrent> find(Collection<Long> torrentIds);

    Torrent find(long torrentId);
}
