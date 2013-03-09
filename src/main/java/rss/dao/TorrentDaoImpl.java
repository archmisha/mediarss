package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.Torrent;
import rss.entities.User;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class TorrentDaoImpl extends BaseDaoJPA<Torrent> implements TorrentDao {

	@Override
	public Collection<Torrent> find(Set<String> titles) {
		if (titles.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder query = new StringBuilder();
		List<Object> params = new ArrayList<>();

		String orPart = " or ";
		query.append("select t from Torrent as t where ");
		int counter = 0;
		for (String title : titles) {
			query.append("(t.title = :p").append(counter++).append(")");
			query.append(orPart);
			params.add(title);
		}
		query.delete(query.length() - orPart.length(), query.length());

		return find(query.toString(), params.toArray());
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

	@Override
	public Torrent findByUrl(String url) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("url", url);
		return uniqueResult(super.<Torrent>findByNamedQueryAndNamedParams("Torrent.findByUrl", params));
	}

	@Override
	public Collection<Torrent> findByIds(Set<Long> torrentIds) {
		Collection<Torrent> result = new ArrayList<>();
		for (Long torrentId : torrentIds) {
			result.add(super.find(torrentId));
		}
		return result;
	}
}
