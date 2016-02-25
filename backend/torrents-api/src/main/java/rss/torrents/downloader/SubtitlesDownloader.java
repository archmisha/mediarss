package rss.torrents.downloader;

import rss.torrents.Subtitles;
import rss.torrents.requests.subtitles.SubtitlesRequest;

import java.util.Set;

/**
 * User: dikmanm
 * Date: 17/10/2015 11:16
 */
public interface SubtitlesDownloader {
    DownloadResult<Subtitles, SubtitlesRequest> download(Set<SubtitlesRequest> subtitlesRequests, DownloadConfig downloadConfig);
}
