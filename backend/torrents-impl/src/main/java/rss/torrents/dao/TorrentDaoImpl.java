package rss.torrents.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.torrents.Torrent;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class TorrentDaoImpl extends BaseDaoJPA<Torrent> implements TorrentDao {

	@Override
	protected Class<? extends Torrent> getPersistentClass() {
		return TorrentImpl.class;
	}

	@Override
	public Torrent findByHash(String hash) {
		Collection<Torrent> torrents = findByHash(Collections.singleton(hash));
		if (torrents.isEmpty()) {
			return null;
		}
		return torrents.iterator().next();
	}

	@Override
	public Collection<Torrent> findByHash(Set<String> hashes) {
		if (hashes.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder query = new StringBuilder();
		List<Object> params = new ArrayList<>();

		String orPart = " or ";
		query.append("select t from Torrent as t where ");
		int counter = 0;
		for (String hash : hashes) {
			query.append("(t.hash = :p").append(counter++).append(")");
			query.append(orPart);
			params.add(hash);
		}
		query.delete(query.length() - orPart.length(), query.length());

		return find(query.toString(), params.toArray());
	}

//	@Override
//	public Torrent findByUrl(String url) {
//		Map<String, Object> params = new HashMap<>(1);
//		params.put("url", url);
//		return uniqueResult(super.<Torrent>findByNamedQueryAndNamedParams("Torrent.findByUrl", params));
//	}
}
