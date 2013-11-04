package rss.services.subtitles;

import rss.services.requests.subtitles.SubtitlesRequest;

import java.util.Set;

/**
 * User: dikmanm
 * Date: 24/01/13 22:38
 */
public interface SubtitlesService {

	void downloadMissingSubtitles();

	void downloadSubtitlesAsync(Set<SubtitlesRequest> subtitlesRequests);
}
