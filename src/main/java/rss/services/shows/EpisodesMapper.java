package rss.services.shows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import rss.entities.Episode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
* User: dikmanm
* Date: 25/02/13 13:30
*/
class EpisodesMapper {
	private Map<Pair<Integer, Integer>, Episode> episodes;

	public EpisodesMapper(Collection<Episode> episodesList) {
		episodes = new HashMap<>();
		for (Episode episode : episodesList) {
			add(episode);
		}
	}

	public Episode get(int season, int episode) {
		return episodes.get(new ImmutablePair<>(season, episode));
	}

	public void add(Episode episode) {
		episodes.put(new ImmutablePair<>(episode.getSeason(), episode.getEpisode()), episode);
	}
}
