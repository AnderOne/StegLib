package steg.lib.img;

import java.awt.image.BufferedImage;

/**
 * The class of a steganography wrapper for arbitrary bitmap which responsible to encode/decode and
 * generates new 24-bits image containing hidden data.
 */
public class BitMapWrapper {

	/**
	 * Converts input bitmap to 24-bits true color format (for compatibility with main algorithm).
	 *
	 * @param  img input bitmap.
	 * @return True color image.
	 */
	public static BufferedImage toTrueColor(BufferedImage img) {

		BufferedImage tmp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		tmp.getGraphics().drawImage(img, 0, 0, null);

		return tmp;
	}

	/**
	 * Creates a new object using key to randomly shuffle parts of hidden data in the bitmap.
	 *
	 * @param img input image which will be container of hidden data.
	 * @param key private key to encode/decode algorithms.
	 */
	public BitMapWrapper(BufferedImage img, String key) {
		height = img.getHeight();
		width = img.getWidth();
		coder = new Steganography(toTrueColor(img).getRGB(0, 0, width, height, null, 0, width), key);
	}

	/**
	 * Creates a new object. It doesn't use shuffling (all parts are stored sequentially).
	 *
	 * @param img input image which will be container of hidden data.
	 */
	public BitMapWrapper(BufferedImage img) {
		height = img.getHeight();
		width = img.getWidth();
		coder = new Steganography(toTrueColor(img).getRGB(0, 0, width, height, null, 0, width));
	}

	/**
	 * Generates a new 24-bits image from input bitmap with hidden data.
	 *
	 * @return Image which contains hidden data.
	 */
	public final BufferedImage getImage() {

		BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		res.setRGB(0, 0, width, height, coder.dat, 0, width);

		return res;
	}

	/**
	 * Gets steganography object connected with bitmap.
	 *
	 * @return Steganography object.
	 */
	public Steganography getCoder() { return coder; }

	/**
	 * Gets height of the bitmap.
	 */
	public int getHeight() { return height; }

	/**
	 * Gets width of the bitmap.
	 */
	public int getWidth() { return width; }

	//Private fields:
	private Steganography coder;
	private int height;
	private int width;
}
