package rss.dao;

import org.springframework.stereotype.Repository;
import rss.SubtitleLanguage;
import rss.entities.Episode;
import rss.entities.Torrent;
import rss.services.requests.DoubleEpisodeRequest;
import rss.services.requests.EpisodeRequest;
import rss.services.requests.FullSeasonRequest;
import rss.services.requests.SingleEpisodeRequest;

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
			if (episodeRequest instanceof SingleEpisodeRequest) {
				counter = generateSingleEpisodePart(query, params, orPart, counter, episodeRequest.getShow().getId(), episodeRequest.getSeason(), ((SingleEpisodeRequest)episodeRequest).getEpisode());
			} else if (episodeRequest instanceof DoubleEpisodeRequest) {
				counter = generateSingleEpisodePart(query, params, orPart, counter, episodeRequest.getShow().getId(), episodeRequest.getSeason(), ((DoubleEpisodeRequest)episodeRequest).getEpisode1().getEpisode());
				counter = generateSingleEpisodePart(query, params, orPart, counter, episodeRequest.getShow().getId(), episodeRequest.getSeason(), ((DoubleEpisodeRequest)episodeRequest).getEpisode2().getEpisode());
			} else if (episodeRequest instanceof FullSeasonRequest) {
				counter = generateSingleEpisodePart(query, params, orPart, counter, episodeRequest.getShow().getId(), episodeRequest.getSeason(), -1);
			}
		}
		query.delete(query.length() - orPart.length(), query.length());

		return find(query.toString(), params.toArray());
	}

	private int generateSingleEpisodePart(StringBuilder query, List<Object> params, String orPart, int counter,
										  long showId, int season, int episode) {
		query.append("(t.show.id = :p").append(counter++).append(" and t.season = :p").append(counter++).append(" and t.episode = :p").append(counter++).append(")");
		query.append(orPart);
		params.add(showId);
		params.add(season);
		params.add(episode);
		return counter;
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

	@Override
	public Collection<Episode> getEpisodesForSchedule(List<Long> showIds) {
		if (showIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Object> params = new ArrayList<>();
		params.add(getScheduleLowerDate());
		params.add(getScheduleUpperDate());
		params.addAll(showIds);

		StringBuilder query = new StringBuilder();
		query.append("select t from Episode as t where :p0 <= t.airDate and t.airDate <= :p1 and t.show.id in (").append(generateQuestionMarks(showIds.size(), 2)).append(")");

		return find(query.toString(), params.toArray());
	}

	private Date getScheduleUpperDate() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_YEAR, 7);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	private Date getScheduleLowerDate() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_YEAR, -7);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
}
