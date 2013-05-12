package rss.services.requests;

import rss.entities.Episode;
import rss.entities.Torrent;

import java.util.Collections;
import java.util.List;

/**
 * User: dikmanm
 * Date: 11/05/13 15:11
 */
public class SubtitlesEpisodeRequest extends SubtitlesRequest {

	private Episode episode;


	public SubtitlesEpisodeRequest(Torrent torrent, Episode episodes, ShowRequest showRequest) {
		super(torrent, showRequest);
		this.episode = episodes;
	}

	public Episode getEpisode() {
		return episode;
	}

	@Override
	public List<Episode> getEpisodes() {
		return Collections.singletonList(episode);
	}
}
