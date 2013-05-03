package rss.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * User: Michael Dikman
 * Date: 10/12/11
 * Time: 20:11
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = -2946773099138138776L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected long id;

	@Column(name = "created")
	private Date created;

	protected BaseEntity() {
		created = new Date();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
}
