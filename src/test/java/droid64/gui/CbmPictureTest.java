package droid64.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class CbmPictureTest {

	@Test
	public void testPlainKoala() throws IOException {
		byte[] bytes = new byte[10003];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) i;
		}
		CbmPicture pic = new CbmPicture(bytes, "TEST");
		BufferedImage img = pic.getImage();
		Assert.assertEquals("TEST", pic.getName());
		Assert.assertNotNull("Image ", img);
	}

	@Test
	public void testCompressedKoala() throws IOException {
		byte[] bytes = new byte[4096];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) i;
		}
		CbmPicture pic = new CbmPicture(bytes, "TEST");
		BufferedImage img = pic.getImage();
		Assert.assertNotNull("Image ", img);
	}

	@Test
	public void testNull() throws IOException {
		new CbmPicture(null, "TEST");
		CbmPicture pic = new CbmPicture(new byte[0], "TEST");
		BufferedImage img = pic.getImage();
		Assert.assertNotNull("Image ", img);
	}

}
