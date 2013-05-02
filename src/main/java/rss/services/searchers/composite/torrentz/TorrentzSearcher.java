package rss.services.searchers.composite.torrentz;

import org.springframework.beans.factory.annotation.Autowired;
import rss.entities.Media;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.services.searchers.composite.CompositeTorrentSearcher;

import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 22:08
 * <p/>
 * <dl><dt><a href="/8683cfd2d2bea23dd137d806cb942755345a5e4d">Premium Rush 2012 1080p Blu ray Remux AVC DTS <b>HD</b> MA 5 1 KRaLiMaRKo
 * </a> &#187; bluray remux <b>movies</b> <b>hd</b> <b>video</b> <b>highres</b> x264</dt>
 * <dd><span class="v" style="color: #A2EB80" title="8">1</span><span class="a"><span title="Sun, 02 Dec 2012 00:06:45">19 hours</span></span>
 * <span class="s">17 GB</span> <span class="u">8</span><span class="d">128</span></dd></dl>
 */
public abstract class TorrentzSearcher<T extends MediaRequest, S extends Media> extends CompositeTorrentSearcher<T, S> {

	@Autowired
	protected TorrentzParser torrentzParser;

	@Override
	protected SearchResult performSearch(T mediaRequest, SimpleTorrentSearcher<T, S> torrentSearcher) {
		return torrentSearcher.searchById(mediaRequest);
	}

	@Override
	public SearchResult search(T mediaRequest) {
		String url = null;
		try {
			url = getSearchUrl(mediaRequest);
		} catch (UnsupportedEncodingException e) {
			logService.error(getClass(), "Failed encoding: " + url + " error: " + e.getMessage(), e);
//			return SearchResult.createNotFound();
		}

		Set<TorrentzResult> torrentzResults = torrentzParser.downloadByUrl(url);

		return processTorrentzResults(mediaRequest, torrentzResults);
	}

	@Override
	public String getName() {
		return TorrentzParserImpl.HOST_NAME;
	}

	protected abstract String getSearchUrl(T mediaRequest) throws UnsupportedEncodingException;

	protected abstract SearchResult processTorrentzResults(T originalRequest, Set<TorrentzResult> foundRequests);
}
