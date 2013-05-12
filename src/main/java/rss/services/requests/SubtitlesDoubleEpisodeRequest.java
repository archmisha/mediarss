package rss.services.requests;

import rss.entities.Episode;
import rss.entities.Torrent;

import java.util.List;

/**
 * User: dikmanm
 * Date: 11/05/13 20:14
 */
public class SubtitlesDoubleEpisodeRequest extends SubtitlesRequest {

	private List<Episode> episodes;

	public SubtitlesDoubleEpisodeRequest(Torrent torrent, List<Episode> episodes, ShowRequest showRequest) {
		super(torrent, showRequest);
		this.episodes = episodes;
	}

	@Override
	public List<Episode> getEpisodes() {
		return episodes;
	}
}
