package rss.services.searchers.composite.torrentz;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 01/05/13 23:26
 */
public interface TorrentzParser {

	Collection<TorrentzResult> downloadByUrl(String url);
}
