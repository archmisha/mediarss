package rss.services.requests;

import rss.entities.Episode;
import rss.entities.Torrent;
import rss.services.subtitles.SubtitleLanguage;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 11/05/13 15:10
 */
public abstract class SubtitlesRequest implements SearchRequest {

	private Torrent torrent;
	private List<SubtitleLanguage> languages;
	private MediaRequest mediaRequest;

	public SubtitlesRequest(Torrent torrent, MediaRequest mediaRequest) {
		this.torrent = torrent;
		this.mediaRequest = mediaRequest;
		this.languages = new ArrayList<>();
	}

	public Torrent getTorrent() {
		return torrent;
	}

	public List<SubtitleLanguage> getLanguages() {
		return languages;
	}

	public abstract List<Episode> getEpisodes();

	public MediaRequest getMediaRequest() {
		return mediaRequest;
	}
}
