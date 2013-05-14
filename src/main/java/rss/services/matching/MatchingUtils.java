package rss.services.matching;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import rss.services.requests.SubtitlesRequest;
import rss.services.shows.ShowServiceImpl;

import java.util.*;

/**
 * User: dikmanm
 * Date: 14/05/13 10:23
 */
public class MatchingUtils {

	public static MatchCandidate filterByLevenshteinDistance(String title, Collection<MatchCandidate> candidates) {
		// filter matching results
		String normalizedTitle = ShowServiceImpl.normalize(title);
		List<Pair<Integer, MatchCandidate>> pairs = new ArrayList<>();
		for (MatchCandidate candidate : candidates) {
			pairs.add(new ImmutablePair<>(StringUtils.getLevenshteinDistance(candidate.getText().trim(), normalizedTitle), candidate));
		}

		MatchCandidate bestCandidate = null;
		if (!pairs.isEmpty()) {
			Collections.sort(pairs, new Comparator<Pair<Integer, MatchCandidate>>() {
				@Override
				public int compare(Pair<Integer, MatchCandidate> o1, Pair<Integer, MatchCandidate> o2) {
					return Ints.compare(o1.getKey(), o2.getKey());
				}
			});

			bestCandidate = pairs.get(0).getValue();
		}
		return bestCandidate;
	}
}
