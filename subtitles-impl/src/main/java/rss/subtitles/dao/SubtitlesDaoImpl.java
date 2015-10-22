package rss.subtitles.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.torrents.Show;
import rss.torrents.Subtitles;
import rss.torrents.Torrent;
import rss.torrents.dao.TorrentImpl;
import rss.torrents.requests.subtitles.SubtitlesDoubleEpisodeRequest;
import rss.torrents.requests.subtitles.SubtitlesMovieRequest;
import rss.torrents.requests.subtitles.SubtitlesRequest;
import rss.torrents.requests.subtitles.SubtitlesSingleEpisodeRequest;
import rss.user.subtitles.SubtitleLanguage;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class SubtitlesDaoImpl extends BaseDaoJPA<Subtitles> implements SubtitlesDao {

	@Override
	protected Class<? extends Subtitles> getPersistentClass() {
		return SubtitlesImpl.class;
	}

	@Override
	public List<Subtitles> find(SubtitlesRequest subtitlesRequest, SubtitleLanguage language) {
		if (subtitlesRequest instanceof SubtitlesMovieRequest) {
			Map<String, Object> params = new HashMap<>(2);
			params.put("torrentId", subtitlesRequest.getTorrent().getId());
			params.put("language", language);
			return super.findByNamedQueryAndNamedParams("Subtitles.find", params);
		} else if (subtitlesRequest instanceof SubtitlesSingleEpisodeRequest) {
			SubtitlesSingleEpisodeRequest sser = (SubtitlesSingleEpisodeRequest) subtitlesRequest;
			String query = "select t from Subtitles as t where t.language = :p0 and t.season = :p1 and t.episode = :p2 and t.episode2 is null";
			return super.find(query, language, sser.getSeason(), sser.getEpisode());
		} else if (subtitlesRequest instanceof SubtitlesDoubleEpisodeRequest) {
			SubtitlesDoubleEpisodeRequest sder = (SubtitlesDoubleEpisodeRequest) subtitlesRequest;
			String query = "select t from Subtitles as t where t.language = :p0 and t.season = :p1 and t.episode = :p2 and t.episode2 = :p3";
			return super.find(query, language, sder.getSeason(), sder.getEpisode1(), sder.getEpisode2());
		} else {
			throw new IllegalArgumentException("Cannot search for requests of type: " + subtitlesRequest.getClass());
		}
	}

	@Override
	public Collection<Subtitles> findByTorrent(Torrent torrent) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("torrentId", torrent.getId());
		return super.findByNamedQueryAndNamedParams("Subtitles.findByTorrent", params);
	}

	@Override
	// languages are prioritized. if torrent subs found with language 1 then this torrent wont be searched for the other languages
	public Collection<Subtitles> find(Set<Torrent> torrents, SubtitleLanguage subtitleLanguage) {
		if (torrents.isEmpty()) {
			return Collections.emptyList();
		}

		Set<Long> ids = new HashSet<>();
		for (Torrent torrent : torrents) {
			ids.add(torrent.getId());
		}

		Map<String, Object> params = new HashMap<>(2);
		params.put("subtitlesLanguage", subtitleLanguage);
		params.put("torrentIds", ids);
		return super.findByNamedQueryAndNamedParams("Subtitles.getSubtitlesForTorrents", params);
	}

	@Override
	public List<SubtitleLanguage> getSubtitlesLanguagesForTorrent(Torrent torrent) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("torrentId", torrent.getId());
		return super.findByNamedQueryAndNamedParams("Subtitles.getSubtitlesLanguagesForTorrent", params);
	}

	@Override
	public List<SubtitleLanguage> getSubtitlesLanguages(Show show) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("showId", show.getId());
		return super.findByNamedQueryAndNamedParams("Subtitles.getSubtitlesLanguages", params);
	}

	@Override
	public Subtitles findByName(String name) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("name", name);
		return uniqueResult(super.<Subtitles>findByNamedQueryAndNamedParams("Subtitles.findByName", params));
	}

	@Override
	public SubtitlesScanHistoryImpl findSubtitleScanHistory(Torrent torrent, SubtitleLanguage language) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("torrentId", torrent.getId());
		params.put("language", language);
		return uniqueResult(super.<SubtitlesScanHistoryImpl>findByNamedQueryAndNamedParams("SubtitlesScanHistoryImpl.find", params));
	}
}
