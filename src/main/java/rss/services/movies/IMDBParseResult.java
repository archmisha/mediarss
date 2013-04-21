package rss.services.movies;

/**
 * User: dikmanm
 * Date: 20/04/13 16:23
 */
public class IMDBParseResult {
	private boolean found;
	private String imdbUrl;
	private String name;
	private int year;
	private boolean comingSoon;
	private int viewers;

	public boolean isFound() {
		return found;
	}

	public static IMDBParseResult createNotFound(String imdbUrl) {
		return new IMDBParseResult().withFound(false).widthImdbUrl(imdbUrl);
	}

	public static IMDBParseResult createFound(String imdbUrl, String name, int year, boolean comingSoon, int viewers) {
		return new IMDBParseResult().withFound(true).widthImdbUrl(imdbUrl).withName(name).withYear(year).withComingSoon(comingSoon).withViewers(viewers);
	}

	private IMDBParseResult withViewers(int viewers) {
		this.viewers = viewers;
		return this;
	}

	private IMDBParseResult withComingSoon(boolean comingSoon) {
		this.comingSoon = comingSoon;
		return this;
	}

	private IMDBParseResult withYear(int year) {
		this.year = year;
		return this;
	}

	private IMDBParseResult withName(String name) {
		this.name = name;
		return this;
	}

	private IMDBParseResult widthImdbUrl(String imdbUrl) {
		this.imdbUrl = imdbUrl;
		return this;
	}

	private IMDBParseResult withFound(boolean found) {
		this.found = found;
		return this;
	}

	public String getName() {
		return name;
	}

	public int getYear() {
		return year;
	}

	public boolean isComingSoon() {
		return comingSoon;
	}

	public int getViewers() {
		return viewers;
	}
}
