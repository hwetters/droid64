package droid64.gui;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

/**
 * <pre style='font-family:sans-serif;'>
 * Created on 2015-Oct-17
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2015 Henrik Wetterström
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *   http://droid64.sourceforge.net
 *
 * &#64;author Henrik
 * </pre>
 */
public class CbmPicture {

	/** Image data */
	private final byte[] data;
	private final String name;
	/** C64 color map */
	private static final int[] COLORS = {
			0x000000, 0xffffff, 0x68372b, 0x70a4b2, 0x6f3d86, 0x588d43, 0x352879, 0xb8c76f,
			0x6f4f25, 0x433900, 0x9a6759, 0x444444, 0x6c6c6c, 0x9ad284, 0x6c5eb5, 0x959595 };

	private static final int KOALA_COLOR_RAM = 0x2328;
	private static final int KOALA_SCREEN_RAM = 0x1f40;
	private static final int KOALA_BACKGROUND = 0x2710;
	private static final int KOALA_RLE_BYTE = 0xfe;
	private static final int KOALA_SIZE = 10003;

	/**
	 * Constructor.
	 * Supports Koala (uncompressed and compressed), JPEG, GIF and PNG.
	 * @param data image data
	 * @param name name of picture
	 */
	public CbmPicture(byte[] data, String name) {
		this.name = name;
		if (data == null || data.length == 0) {
			this.data = new byte[0];
		} else if (data.length < KOALA_SIZE && !isGif(data) && !isJpeg(data) && !isPng(data)) {
			this.data = decompress(data);
		} else {
			this.data = data;
		}
	}

	/**
	 * @return BufferedImage of the image data.
	 * @throws IOException when error
	 */
	public BufferedImage getImage() throws IOException {
		BufferedImage image;
		if (isGif(data) || isJpeg(data) || isPng(data)) {
			InputStream in = new ByteArrayInputStream(data);
			image = ImageIO.read(in);
			in.close();
		} else {
			image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
			boolean colorMode = true;
			int[] colors = new int[4];
			if (data.length < KOALA_SIZE) {
				return image;
			}
			int background = data[KOALA_BACKGROUND + 2] & 0x0f;
			colors[0] = COLORS[background];
			IntStream.range(0, 25).forEach(row ->
				IntStream.range(0, 40).forEach(col -> {
					int screenColor = data[2 + KOALA_SCREEN_RAM + row * 40 + col] & 0xff;
					int colorRam = data[2 + KOALA_COLOR_RAM + row * 40 + col] & 0x0f;
					colors[1] = COLORS[screenColor >>> 4];
					colors[2] = COLORS[screenColor & 0x0f];
					colors[3] = COLORS[colorRam];
					IntStream.range(0, 8).forEach(k -> setPixelOctet(row, col, k, colorMode, colors, image));
				})
			);
		}
		return image;
	}

	private void setPixelOctet(int row, int col, int k, boolean colorMode, int[] colors, BufferedImage image) {
		int b = data[2 + ((row * 40 + col) << 3) + k] & 0xff;
		int x = col << 3;
		int y = (row << 3) + k;
		if (colorMode) {
			setPixelOctet1(x,y , b, colors, image);
		} else {
			setPixelOctet2(x,y , b, colors, image);
		}
	}

	private void setPixelOctet1(int x, int y, int b, int[] colors, BufferedImage image) {
		int c0 = (b >>> 6) & 0x03;
		int c1 = (b >>> 4) & 0x03;
		int c2 = (b >>> 2) & 0x03;
		int c3 = b & 0x03;
		image.setRGB(x++, y, colors[c0]);
		image.setRGB(x++, y, colors[c0]);
		image.setRGB(x++, y, colors[c1]);
		image.setRGB(x++, y, colors[c1]);
		image.setRGB(x++, y, colors[c2]);
		image.setRGB(x++, y, colors[c2]);
		image.setRGB(x++, y, colors[c3]);
		image.setRGB(x, y, colors[c3]);
	}

	private void setPixelOctet2(int x, int y, int b, int[] colors, BufferedImage image) {
		image.setRGB(x++, y, colors[(b & 0x80) == 0 ? 0 : 1]);
		image.setRGB(x++, y, colors[(b & 0x40) == 0 ? 0 : 1]);
		image.setRGB(x++, y, colors[(b & 0x20) == 0 ? 0 : 1]);
		image.setRGB(x++, y, colors[(b & 0x10) == 0 ? 0 : 1]);
		image.setRGB(x++, y, colors[(b & 0x08) == 0 ? 0 : 1]);
		image.setRGB(x++, y, colors[(b & 0x04) == 0 ? 0 : 1]);
		image.setRGB(x++, y, colors[(b & 0x02) == 0 ? 0 : 1]);
		image.setRGB(x, y, colors[(b & 0x01) == 0 ? 0 : 1]);
	}

	/**
	 * Decompress Koala
	 *
	 * @param inData
	 * @return image data
	 */
	private byte[] decompress(byte[] inData) {
		var outData = new byte[KOALA_SIZE];
		int in = 0;
		int out = 0;
		while (in < inData.length && out < outData.length) {
			int b = inData[in++] & 0xff;
			if (b == KOALA_RLE_BYTE) {
				byte v = inData[in++];
				int c = in < inData.length ? inData[in++] & 0xff : 0;
				if (c == 0) {
					c = 256;
				}
				for (int j = 0; j < c && out < outData.length; j++) {
					outData[out++] = v;
				}
			} else {
				outData[out++] = (byte) b;
			}
		}
		return outData;
	}

	private boolean byteArrayStartsWith(byte[] data, byte[] compare) {
		if (data.length < compare.length) {
			return false;
		}
		for (int i=0; i< compare.length; i++) {
			if (data[i] != compare[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean isGif(byte[] data) {
		final byte[] gifMatch1 = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x37, 0x61 };
		final byte[] gifMatch2 = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 };
		return byteArrayStartsWith(data, gifMatch1) || byteArrayStartsWith(data, gifMatch2);
	}

	private boolean isJpeg(byte[] data) {
		if (data.length < 4) {
			return false;
		}
		return (data[0] & 0xff) == 0xff && (data[1] & 0xff) == 0xd8
				&& (data[data.length - 2] & 0xff) == 0xff && (data[data.length - 1] & 0xff) == 0xd9;
	}

	private boolean isPng(byte[] data) {
		final byte[] pngMatch = new byte[] { (byte)(0x89 & 0xff), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a };
		return byteArrayStartsWith(data, pngMatch);
	}

	public String getName() {
		return name;
	}

}
