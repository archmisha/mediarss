package rss.entities;

import org.hibernate.annotations.Index;
import rss.ems.entities.BaseEntity;

import javax.persistence.*;

/**
 * User: dikmanm
 * Date: 07/01/14 18:47
 */
@Entity
@Table(name = "searcher_conf")
@org.hibernate.annotations.Table(appliesTo = "searcher_conf",
		indexes = {
				@Index(name = "searcher_conf_name_idx", columnNames = {"name"})
		})
@NamedQueries({
		@NamedQuery(name = "SearcherConfigurationEntity.findByName",
				query = "select b from SearcherConfigurationEntity as b where b.name = :name")
})
public class SearcherConfigurationEntity extends BaseEntity {
	private static final long serialVersionUID = 3207350638947517834L;

	@Column(name = "name")
	private String name;

	@Column(name = "dns")
	private String dns;

	public SearcherConfigurationEntity() {
	}

	public String getDns() {
		return dns;
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
