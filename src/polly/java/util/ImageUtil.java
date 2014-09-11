package polly.java.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import javax.swing.ImageIcon;

public class ImageUtil {
	
	public static boolean hasAlpha(Image image) {
		if (image == null) return false;
		
		if (image instanceof BufferedImage) {
			BufferedImage buffImg = (BufferedImage) image;
			return buffImg.getColorModel().hasAlpha();
		}
		
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
			return pg.getColorModel().hasAlpha();
		} catch (InterruptedException e) {
			
		}
		
		return true;
	}
	
	public static BufferedImage toBufferedImage(Image image) {
		if (image == null) return null;
		
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		if (image.getWidth(null) < 0 || image.getHeight(null) < 0) {
			// This code ensures that all the pixels in the image are loaded
			image = new ImageIcon(image).getImage();
		}

		int imgType = hasAlpha(image) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage buffImg = new BufferedImage(image.getWidth(null), image.getHeight(null), imgType);
		Graphics g = buffImg.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return buffImg;
	}
	
	public static Image scaleImage(Image image, float scale) {
		if (image == null) return null;
		
		BufferedImage sourceImage = toBufferedImage(image);
		int scaledWidth = (int) (sourceImage.getWidth() * scale);
		if (scaledWidth <= 0) return null;
		int scaledHeight = (int) (sourceImage.getHeight() * scale);
		if (scaledHeight <= 0) return null;
		
		BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, sourceImage.getType());
		AffineTransform at = new AffineTransform();
		at.scale(scale, scale);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		scaledImage = scaleOp.filter(sourceImage, scaledImage);
		
		return scaledImage;
	}
}
