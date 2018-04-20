import java.awt.image.BufferedImage;
import java.io.IOException;
import steg.lib.img.BitMapWrapper;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestForBitMapWrapper {

	@Test
	public void TestForBitMapWrapperCoder() throws IOException {

		BufferedImage img = randImage(128, 256, BufferedImage.TYPE_INT_RGB);
		String key = "key";
		BitMapWrapper map = new BitMapWrapper(img, key);
		byte dat[] = randBytes(1024);

		map.getCoder().encode(dat);

		map = new BitMapWrapper(map.getImage(), key);

		byte out[] = map.getCoder().decode();

		Assert.assertArrayEquals(dat, out);
	}

	private static BufferedImage randImage(int w, int h, int type) {

		BufferedImage img = new BufferedImage(w, h, type);
		Random rnd = new Random();
		int rgb[] = img.getRGB(0, 0, w, h, null, 0, w);
		for (int i = 0; i < rgb.length; ++ i) rgb[i] = rnd.nextInt();
		img.setRGB(0, 0, w, h, rgb, 0, w);

		return  img;
	}

	private static byte[] randBytes(int n) {

		Random rnd = new Random();
		byte out[] = new byte[n];
		rnd.nextBytes(out);

		return  out;
	}

}
