package rss.entities;

import org.hibernate.annotations.Index;
import rss.ems.entities.BaseEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 12/12/12
 * Time: 23:13
 */
@Entity
@Table(name = "job_status")
@NamedQueries({
        @NamedQuery(name = "JobStatus.findByName",
                query = "select t from JobStatus as t where t.name = :name")
})
public class JobStatus extends BaseEntity {

    private static final long serialVersionUID = -2177939206974795031L;

    @Column(name = "name")
    @Index(name = "name_idx")
    private String name;

    @Column(name = "start")
    private Date start;

    @Column(name = "end")
    private Date end;

    @Column(name = "error")
	@Lob
    private String errorMessage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
