package rss.dao;

import rss.entities.Torrent;

import java.util.Collection;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface TorrentDao extends Dao<Torrent> {

//    Collection<Torrent> find(Set<String> titles);

	Collection<Torrent> findByHash(Set<String> hashes);

	Torrent findByUrl(String url);
}
