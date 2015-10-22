package rss.torrents.searchers;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 01/05/13 23:26
 */
public interface TorrentzParser {

    Collection<TorrentzResult> downloadByUrl(String url);

    Collection<TorrentzResult> downloadMoviesByAge(int days);
}
