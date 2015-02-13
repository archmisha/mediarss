package mediarss.test.entities;

/**
 * User: dikmanm
 * Date: 13/02/2015 12:22
 */
public class UserLoginResult {
    private boolean isAdmin;
    private long deploymentDate;
    private String firstName;
    private String tvShowsRssFeed;
    private String moviesRssFeed;
    private News[] news;

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public long getDeploymentDate() {
        return deploymentDate;
    }

    public void setDeploymentDate(long deploymentDate) {
        this.deploymentDate = deploymentDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getTvShowsRssFeed() {
        return tvShowsRssFeed;
    }

    public void setTvShowsRssFeed(String tvShowsRssFeed) {
        this.tvShowsRssFeed = tvShowsRssFeed;
    }

    public String getMoviesRssFeed() {
        return moviesRssFeed;
    }

    public void setMoviesRssFeed(String moviesRssFeed) {
        this.moviesRssFeed = moviesRssFeed;
    }

    public News[] getNews() {
        return news;
    }

    public void setNews(News[] news) {
        this.news = news;
    }
}
