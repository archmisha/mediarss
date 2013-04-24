package rss.entities;

import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 24/04/13 23:02
 */
@Entity
@Table(name = "image")
@NamedQueries({
		@NamedQuery(name = "Image.findByKey",
				query = "select t from Image as t where t.key = :key")
})
public class Image extends BaseEntity  {

	private static final long serialVersionUID = 8943725753545525673L;

	@Column(name = "key", unique = true)
	@Index(name = "image_key_idx")
	private String key;

	@Column(name = "data")
	@Lob
	private byte[] data;

	@Column(name = "created")
	private Date created;

	public Image() {
		this.created = new Date();
	}

	public Image(String key, byte[] data) {
		this();
		this.key = key;
		this.data = data;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
