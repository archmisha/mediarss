package rss.services.subtitles;

import rss.entities.Subtitles;
import rss.entities.Episode;
import rss.entities.Torrent;
import rss.services.requests.SubtitlesEpisodeRequest;
import rss.services.requests.SubtitlesRequest;

import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 24/01/13 22:38
 */
public interface SubtitlesService {



	void downloadMissingSubtitles();

	void downloadSubtitlesAsync(List<SubtitlesRequest> subtitlesRequests);
}
