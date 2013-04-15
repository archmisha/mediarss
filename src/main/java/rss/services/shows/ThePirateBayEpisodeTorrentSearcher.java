package rss.services.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.entities.Torrent;
import rss.services.requests.EpisodeRequest;
import rss.services.searchers.ThePirateBayTorrentSearcher;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 13/04/13 14:26
 */
@Service("thePirateBayEpisodeTorrentSearcher")
public class ThePirateBayEpisodeTorrentSearcher extends ThePirateBayTorrentSearcher<EpisodeRequest, Episode> {

	@Autowired
	private ShowService showService;

	@Override
	protected void verifySearchResults(EpisodeRequest mediaRequest, List<Torrent> results) {
		for (Torrent torrent : new ArrayList<>(results)) {
			if (!showService.isMatch(mediaRequest, torrent.getTitle())) {
				results.remove(torrent);
				logService.info(getClass(), "Removing '" + torrent.getTitle() + "' cuz a bad match for '" + mediaRequest.toString() + "'");
			}
		}
	}
}
