package rss.services.requests;

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
	private String name;

	public SubtitlesRequest(Torrent torrent, String name, List<SubtitleLanguage> languages) {
		this.torrent = torrent;
		this.name = name;
		this.languages = new ArrayList<>(languages);
	}

	public Torrent getTorrent() {
		return torrent;
	}

	public List<SubtitleLanguage> getLanguages() {
		return languages;
	}

	public String getName() {
		return name;
	}
}
