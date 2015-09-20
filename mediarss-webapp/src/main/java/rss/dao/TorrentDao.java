package rss.dao;

import rss.ems.dao.Dao;
import rss.torrents.Torrent;

import java.util.Collection;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface TorrentDao extends Dao<Torrent> {

	Torrent findByHash(String hash);

	Collection<Torrent> findByHash(Set<String> hashes);
}
