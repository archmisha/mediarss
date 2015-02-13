package rss.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 07/01/14 18:26
 */
public class SearcherConfiguration {

	private long id;
	private String name;
	private Set<String> dns;

	public SearcherConfiguration() {
		dns = new HashSet<>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Set<String> getDomains() {
		return dns;
	}

	public void setDns(Set<String> dns) {
		this.dns = dns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
