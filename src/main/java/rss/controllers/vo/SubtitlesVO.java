package rss.controllers.vo;

/**
 * User: dikmanm
 * Date: 06/01/14 01:18
 */
public class SubtitlesVO {
	private long id;
	private String type;
	private String name;
	private String language;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
