package rss.entities;

import org.hibernate.annotations.Index;
import rss.ems.entities.BaseEntity;

import javax.persistence.*;

/**
 * User: dikmanm
 * Date: 14/05/13 18:25
 */
@Entity
@Table(name = "view", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "object_id"}))
@org.hibernate.annotations.Table(appliesTo = "view",
		indexes = {
				@Index(name = "view_userId_objectId_idx", columnNames = {"user_id", "object_id"})
		})
@NamedQueries({
		@NamedQuery(name = "View.findByObjectId",
				query = "select v from View as v " +
						"where v.user.id = :userId and v.objectId = :objectId")
})
public class View extends BaseEntity {

	private static final long serialVersionUID = 6293220222526358745L;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "object_id", nullable = false)
	private long objectId;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}
}
