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
		int result = o1.getName().compareTo(o2.getName());
		if (result != 0) {
			return result;
		}

		result = new Integer(o1.getSeason()).compareTo(o2.getSeason());
		if (result != 0) {
			return result;
		}

		return new Integer(o1.getEpisode()).compareTo(o2.getEpisode());
	}
}
