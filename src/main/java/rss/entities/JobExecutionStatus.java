package rss.entities;

import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 22:02
 */
public class JobExecutionStatus {

	private String name;
    private Date startDate;
    private Date endDate;
    private boolean running;
    private String status;

	public JobExecutionStatus(String name) {
		this.name = name;
	}

	public void setRunning(boolean running) {
        this.running = running;
        if (running) {
            startDate = new Date();
        } else {
            endDate = new Date();
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRunning() {
        return running;
    }

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}
}
