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
public class EpisodesMapper {
	private Map<Pair<Integer, Integer>, Episode> episodes;

	public EpisodesMapper() {
		episodes = new HashMap<>();
	}

	public EpisodesMapper(Collection<Episode> episodesList) {
		this();
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

	public void add(Collection<Episode> episodes) {
		for (Episode episode : episodes) {
			add(episode);
		}
	}
}
