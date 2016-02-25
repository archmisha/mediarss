package rss.movies.imdb;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 20/04/13 16:23
 */
public class IMDBParseResult {
	private boolean found;
	private String name;
	private int year;
	private boolean comingSoon;
	private int viewers;
	private Date releaseDate;
	private String page;

	public boolean isFound() {
		return found;
	}

	public static IMDBParseResult createNotFound() {
		return new IMDBParseResult().withFound(false);
	}

	public static IMDBParseResult createFound(String name, int year, boolean comingSoon, int viewers, Date releaseDate, String page) {
		return new IMDBParseResult().withFound(true).withName(name).withYear(year)
				.withComingSoon(comingSoon).withViewers(viewers).withReleaseDate(releaseDate).withPage(page);
	}

	private IMDBParseResult withViewers(int viewers) {
		this.viewers = viewers;
		return this;
	}

	private IMDBParseResult withPage(String page) {
		this.page = page;
		return this;
	}

	private IMDBParseResult withReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
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

	public Date getReleaseDate() {
		return releaseDate;
	}

	public String getPage() {
		return page;
	}
}
