package rss.subtitles;

import rss.torrents.Subtitles;

/**
 * User: dikmanm
 * Date: 17/10/2015 16:28
 */
public interface SubtitlesServiceFactory {
    Subtitles createMovieSubtitles();

    Subtitles createEpisodeSubtitles(int season, int episode);

    Subtitles createDoubleEpisodeSubtitles(int season, int episode1, int episode2);

    SubtitlesScanHistory createSubtitlesScanHistory();
}
