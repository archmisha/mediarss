package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.entities.Torrent;
import rss.services.SearchResult;
import rss.services.requests.EpisodeRequest;
import rss.services.shows.ShowService;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 13/04/13 18:25
 */
@Service("kickAssTorrentEpisodeSearcher")
public class KickAssTorrentEpisodeSearcher extends KickAssTorrentSearcher<EpisodeRequest, Episode> {

	@Override
	protected void logNoImdbFound(Torrent torrent) {
		logService.debug(getClass(), "Didn't find IMDB url for: " + torrent.getTitle());
	}
}
