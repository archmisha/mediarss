package rss.movies.images;

/**
 * User: dikmanm
 * Date: 26/04/2014 10:56
 */
public interface ImageService {
	Image getImage(String name);

	void saveImage(Image image);
}
