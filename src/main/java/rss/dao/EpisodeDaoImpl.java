package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.Episode;
import rss.services.EpisodeRequest;
import rss.SubtitleLanguage;
import rss.entities.Torrent;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class EpisodeDaoImpl extends BaseDaoJPA<Episode> implements EpisodeDao {

	@Override
	public Episode find(EpisodeRequest episodeRequest) {
		return uniqueResult(find(Collections.singletonList(episodeRequest)));
	}

	@Override
	public List<Episode> find(Collection<EpisodeRequest> episodeRequests) {
		if (episodeRequests.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder query = new StringBuilder();
		List<Object> params = new ArrayList<>();

		String orPart = " or ";
		query.append("select t from Episode as t where ");
		int counter = 0;
		for (EpisodeRequest episodeRequest : episodeRequests) {
			query.append("(lower(t.show.name) = :p").append(counter++).append(" and t.season = :p").append(counter++).append(" and t.episode = :p").append(counter++).append(")");
			query.append(orPart);
			params.add(episodeRequest.getTitle().toLowerCase());
			params.add(episodeRequest.getSeason());
			params.add(episodeRequest.getEpisode());

		}
		query.delete(query.length() - orPart.length(), query.length());

		return find(query.toString(), params.toArray());
	}

	@Override
	public List<SubtitleLanguage> getSubtitlesLanguages(Episode episode) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("episodeId", episode.getId());
		return super.findByNamedQueryAndNamedParams("Episode.getSubtitlesLanguages", params);
	}

	@Override
	public Episode find(Torrent torrent) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("torrentId", torrent.getId());
		return uniqueResult(super.<Episode>findByNamedQueryAndNamedParams("Episode.findByTorrent", params));
	}
}
