package rss.services.searchers.composite.torrentz;

import java.util.Set;

/**
 * User: dikmanm
 * Date: 01/05/13 23:26
 */
public interface TorrentzParser {

	Set<TorrentzResult> downloadByUrl(String url);
}
