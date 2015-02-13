package rss;

import rss.entities.Episode;

import java.util.Comparator;

/**
 * User: dikmanm
 * Date: 26/12/12 20:36
 */
public class EpisodesComparator implements Comparator<Episode> {

	@Override
	public int compare(Episode o1, Episode o2) {
		// names come in capital letters and with dots etc, should first compare by season and episode and by name at the end
//		int result = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
//		if (result != 0) {
//			return result;
//		}

		int result = new Integer(o1.getSeason()).compareTo(o2.getSeason());
		if (result != 0) {
			return result;
		}

		result = new Integer(o1.getEpisode()).compareTo(o2.getEpisode());
		if (result != 0) {
			return result;
		}

		return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
	}
}
