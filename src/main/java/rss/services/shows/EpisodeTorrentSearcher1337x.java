package rss.services.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Episode;
import rss.services.EpisodeRequest;
import rss.services.searchers.TorrentSearcher1337x;

/**
 * User: dikmanm
 * Date: 13/04/13 18:25
 */
@Service("episodeTorrentSearcher1337x")
public class EpisodeTorrentSearcher1337x extends TorrentSearcher1337x<EpisodeRequest, Episode> {

	@Autowired
	private ShowService showService;

	@Override
	protected boolean verifySearchResult(EpisodeRequest mediaRequest, String title) {
		return showService.isMatch(mediaRequest, title);
	}
}
