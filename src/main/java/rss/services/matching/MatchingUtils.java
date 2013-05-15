package rss.services.matching;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import rss.services.log.LogService;
import rss.services.shows.ShowServiceImpl;

import java.util.*;

/**
 * User: dikmanm
 * Date: 14/05/13 10:23
 */
public class MatchingUtils {

	public static MatchCandidate filterByLevenshteinDistance(String title, Collection<MatchCandidate> candidates, LogService logService) {
		// filter matching results
		String normalizedTitle = ShowServiceImpl.normalize(title);
		logService.info(MatchingUtils.class, "Calculating LD with '" + normalizedTitle + "'");
		List<Pair<Integer, MatchCandidate>> pairs = new ArrayList<>();
		for (MatchCandidate candidate : candidates) {
			String candidateText = candidate.getText();
			String normalizedCandidateText = ShowServiceImpl.normalize(candidateText);
			int ld = StringUtils.getLevenshteinDistance(normalizedCandidateText, normalizedTitle);
			pairs.add(new ImmutablePair<>(ld, candidate));
			logService.info(MatchingUtils.class, "LD=" + ld + " " + normalizedCandidateText);
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
