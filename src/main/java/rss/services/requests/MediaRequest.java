package rss.services.requests;

import rss.services.searchers.MatcherVisitor;
import rss.services.shows.ShowService;

import java.io.Serializable;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 14:30
 */
public abstract class MediaRequest implements Serializable {

	private static final long serialVersionUID = 5299194875537926970L;

	protected String pirateBayId;
	private String kickAssTorrentsId;
	private String imdbId;
	private String hash;
	private String title;
	private int uploaders;
private int resultsLimit;

	protected MediaRequest(int resultsLimit) {
		this.resultsLimit = resultsLimit;
	}

	protected MediaRequest(String title, int resultsLimit) {
		this(resultsLimit);
		this.title = title;
	}

	protected MediaRequest(String title, String hash, int resultsLimit) {
		this(title, resultsLimit);
		this.hash = hash;
	}

	public int getResultsLimit() {
		return resultsLimit;
	}

	public int getUploaders() {
		return uploaders;
	}

	public void setUploaders(int uploaders) {
		this.uploaders = uploaders;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String toQueryString() {
		return title;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		if (hash != null) {
			sb.append(" (").append(hash).append(")");
		}
		return sb.toString();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPirateBayId() {
		return pirateBayId;
	}

	public void setPirateBayId(String pirateBayId) {
		this.pirateBayId = pirateBayId;
	}

	public void setKickAssTorrentsId(String kickAssTorrentsId) {
		this.kickAssTorrentsId = kickAssTorrentsId;
	}

	public String getKickAssTorrentsId() {
		return kickAssTorrentsId;
	}

	public String getImdbId() {
		return imdbId;
	}

	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MediaRequest that = (MediaRequest) o;

		if (title != null ? !title.equals(that.title) : that.title != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return title != null ? title.hashCode() : 0;
	}

	public abstract List<ShowService.MatchCandidate> visit(MatcherVisitor visitor, List<ShowService.MatchCandidate> matchCandidates);
}
