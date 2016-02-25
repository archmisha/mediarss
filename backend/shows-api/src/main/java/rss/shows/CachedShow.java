package rss.shows;

/**
 * User: dikmanm
 * Date: 15/03/13 10:35
 */
public class CachedShow {

    private long id;
    private String name;
    private String normalizedName;
    private int words;
    private boolean ended;

    public CachedShow(long id, String name, boolean ended) {
        this.id = id;
        this.name = name;
        this.ended = ended;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedShow that = (CachedShow) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }
}
