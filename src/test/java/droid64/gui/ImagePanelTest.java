package droid64.gui;

import java.io.IOException;

import org.junit.Test;

import org.junit.Assert;

public class ImagePanelTest {

	@Test
	public void test() throws IOException {
		ImagePanel imgPanel = new ImagePanel(createCbmPicture());
		imgPanel.setImage(createCbmPicture());
		Assert.assertEquals("name", "TEST", imgPanel.getName());
	}

	private CbmPicture createCbmPicture() {
		byte[] bytes = new byte[10003];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) i;
		}
		return new CbmPicture(bytes, "TEST");
	}
}
