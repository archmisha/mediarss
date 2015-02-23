package rss.rms.driver.transformer;

/**
 * @author Dolev Dori
 *         Date: 20/01/2015
 *         Time: 15:51
 * @since 1.0.0-9999
 */
public class InvalidObjectIdException extends IllegalArgumentException {

    private final String id;

    public InvalidObjectIdException(String id) {
        super("Invalid resource id '" + id + "'");
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
