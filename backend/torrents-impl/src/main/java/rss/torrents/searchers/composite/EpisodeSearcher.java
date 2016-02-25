package rss.torrents.searchers.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.torrents.requests.shows.ShowRequest;
import rss.torrents.searchers.AbstractMediaSearcher;
import rss.torrents.searchers.composite.torrentz.EpisodeTorrentzSearcher;
import rss.torrents.searchers.composite.torrentz.TorrentzSearcher;

/**
 * User: dikmanm
 * Date: 12/05/13 21:30
 */
@Service
public class EpisodeSearcher extends AbstractMediaSearcher<ShowRequest> {

	@Autowired
	@Qualifier("defaultCompositeSearcher")
	private DefaultCompositeSearcher<ShowRequest> episodeCompositeSearcher;

	@Autowired
	private EpisodeTorrentzSearcher episodeTorrentzSearcher;

	@Override
	protected DefaultCompositeSearcher<ShowRequest> getDefaultCompositeSearcher() {
		return episodeCompositeSearcher;
	}

	@Override
	protected TorrentzSearcher<ShowRequest> getTorrentzSearcher() {
		return episodeTorrentzSearcher;
	}
}
