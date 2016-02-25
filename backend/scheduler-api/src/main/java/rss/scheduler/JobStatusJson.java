package rss.scheduler;

import rss.rms.RmsResource;

/**
 * User: Michael Dikman
 * Date: 12/12/12
 * Time: 23:13
 */
public class JobStatusJson implements RmsResource {

    private String id;
    private String name;
    private Long start;
    private Long end;
    private String errorMessage;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
