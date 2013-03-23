package rss.dao;

import org.springframework.stereotype.Repository;
import rss.SubtitleLanguage;
import rss.entities.Subtitles;
import rss.entities.Torrent;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class SubtitlesDaoImpl extends BaseDaoJPA<Subtitles> implements SubtitlesDao {

	@Override
	public Subtitles find(Torrent torrent, SubtitleLanguage language) {
		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("torrentId", torrent.getId());
		params.put("language", language);
		return uniqueResult(super.<Subtitles>findByNamedQueryAndNamedParams("Subtitles.find", params));
	}

	@Override
	// languages are prioritized. if torrent subs found with language 1 then this torrent wont be searched for the other languages
	public Collection<Subtitles> find(Set<Torrent> torrents, SubtitleLanguage... subtitleLanguages) {
		if (torrents.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<Subtitles> result = new ArrayList<>();
		Set<Torrent> torrentsCopy = new HashSet<>(torrents);
		for (SubtitleLanguage subtitleLanguage : subtitleLanguages) {
			List<Object> params = new ArrayList<>();
			params.add(subtitleLanguage);
			for (Torrent torrent : torrentsCopy) {
				params.add(torrent.getId());
			}
			String query = "select t from Subtitles as t where t.language = :p0 and t.torrent.id in (" + generateQuestionMarks(torrentsCopy.size(), 1) + ")";
			List<Subtitles> subs = find(query, params.toArray());
			result.addAll(subs);

			for (Subtitles sub : subs) {
				torrentsCopy.remove(sub.getTorrent());
			}
			if (torrentsCopy.isEmpty()) {
				break;
			}
		}

		return result;
	}
}
