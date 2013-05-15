package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.Episode;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.requests.episodes.*;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class EpisodeDaoImpl extends BaseDaoJPA<Episode> implements EpisodeDao {

	@Override
	public List<Episode> find(EpisodeRequest episodeRequest) {
		return find(Collections.<ShowRequest>singletonList(episodeRequest));
	}

	@Override
	public List<Episode> find(Collection<ShowRequest> episodeRequests) {
		if (episodeRequests.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder query = new StringBuilder();
		List<Object> params = new ArrayList<>();

		String orPart = " or ";
		query.append("select t from Episode as t where ");
		int counter = 0;
		for (ShowRequest episodeRequest : episodeRequests) {
			if (episodeRequest instanceof SingleEpisodeRequest) {
				SingleEpisodeRequest ser = (SingleEpisodeRequest) episodeRequest;
				counter = generateSingleEpisodePart(query, params, orPart, counter, episodeRequest.getShow().getId(), ser.getSeason(), ser.getEpisode());
			} else if (episodeRequest instanceof DoubleEpisodeRequest) {
				DoubleEpisodeRequest der = (DoubleEpisodeRequest) episodeRequest;
				counter = generateSingleEpisodePart(query, params, orPart, counter, episodeRequest.getShow().getId(), der.getSeason(), der.getEpisode1());
				counter = generateSingleEpisodePart(query, params, orPart, counter, episodeRequest.getShow().getId(), der.getSeason(), der.getEpisode2());
			} else if (episodeRequest instanceof FullSeasonRequest) {
				FullSeasonRequest fsr = (FullSeasonRequest) episodeRequest;
				counter = generateSingleEpisodePart(query, params, orPart, counter, episodeRequest.getShow().getId(), fsr.getSeason(), -1);
			} else {
				throw new IllegalArgumentException("Cannot search for requests of type: " + episodeRequest.getClass());
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

	@Override
	public boolean exists(Show show, Episode episode) {
		StringBuilder query = new StringBuilder();
		List<Object> params = new ArrayList<>();

		query.append("select count(t) from Episode as t where ");
		generateSingleEpisodePart(query, params, "", 0, show.getId(), episode.getSeason(), episode.getEpisode());
		return uniqueResult(super.<Long>find(query.toString(), params.toArray())) > 0;
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
