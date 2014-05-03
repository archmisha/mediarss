package rss.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.MediaRSSException;
import rss.entities.Image;
import rss.services.log.LogService;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: dikmanm
 * Date: 26/04/2014 10:56
 */
@Service
public class ImageServiceImpl implements ImageService {

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private LogService logService;

	@Override
	public Image getImage(String name) {
		try {
			BufferedImage bufferedImage = ImageIO.read(new File(getFullImagePath(name)));
			WritableRaster raster = bufferedImage.getRaster();
			DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
			return new Image(name, data.getData());
		} catch (IIOException e) {
			if (e.getMessage().equals("Can't read input file!")) {
				return null;
			}
			throw new MediaRSSException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MediaRSSException(e.getMessage(), e);
		}
	}

	@Override
	public void saveImage(Image image) {
		String path = getFullImagePath(image.getKey());
		logService.info(getClass(), "Storing a new image: " + path);
		try (FileOutputStream fos = new FileOutputStream(path)) {
			fos.write(image.getData());
		} catch (IOException e) {
			throw new MediaRSSException(e.getMessage(), e);
		}
	}

	private String getFullImagePath(String name) {
		String path = settingsService.getImagesPath();
		int i = name.lastIndexOf("/");
		if (i > -1) {
			name = name.substring(i + 1);
		}
		return path + File.separator + name;
	}
}
