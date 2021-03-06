package droid64.d64;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class LNXTest extends DiskImageBaseTest {

	private static short[] HEADER = {
			0x01, 0x08, 0x5b, 0x08, 0x0a, 0x00, 0x97, 0x35, 0x33, 0x32, 0x38, 0x30, 0x2c,
			0x30, 0x3a, 0x97, 0x35, 0x33, 0x32, 0x38, 0x31, 0x2c, 0x30, 0x3a, 0x97, 0x36, 0x34, 0x36, 0x2c, 0xc2, 0x28,
			0x31, 0x36, 0x32, 0x29, 0x3a, 0x99, 0x22, 0x93, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x22, 0x3a,
			0x99, 0x22, 0x20, 0x20, 0x20, 0x20, 0x20, 0x55, 0x53, 0x45, 0x20, 0x4c, 0x59, 0x4e, 0x58, 0x20, 0x54, 0x4f,
			0x20, 0x44, 0x49, 0x53, 0x53, 0x4f, 0x4c, 0x56, 0x45, 0x20, 0x54, 0x48, 0x49, 0x53, 0x20, 0x46, 0x49, 0x4c,
			0x45, 0x22, 0x3a, 0x89, 0x31, 0x30, 0x00, 0x00, 0x00, 0x0d, 0x20, 0x31, 0x20, 0x20, 0x55, 0x4e, 0x49, 0x54,
			0x20, 0x54, 0x45, 0x53, 0x54, 0x0d, 0x20, 0x31, 0x20, 0x0d, 0x46, 0x49, 0x4c, 0x45, 0x0d, 0x20, 0x32, 0x20,
			0x0d, 0x50, 0x0d, 0x20, 0x33, 0x20, 0x0d };

	@Override
	@Test
	public void testToString() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new LNX(consoleStream).toString().isEmpty());
	}

	@Test
	public void test() {
		var consoleStream = new ConsoleStream(new JTextArea());
		byte[] data = new byte[HEADER.length];
		for (int i = 0; i < HEADER.length; i++) {
			data[i] = (byte) HEADER[i];
		}
		LNX lnx = new LNX(data, consoleStream);
		lnx.readBAM();
		lnx.readDirectory();
		lnx.saveFileData(data);
		lnx.setDiskName("name", "id");
		lnx.getBamTable();
		Assert.assertEquals("free", 0,  lnx.getBlocksFree());
	}

	@Test(expected=CbmException.class)
	public void testNoData() throws CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		byte[] data = new byte[HEADER.length];
		for (int i = 0; i < HEADER.length; i++) {
			data[i] = (byte) HEADER[i];
		}
		LNX lnx = new LNX(data, consoleStream);
		lnx.getFileData(99);
	}

	@Test
	public void testLNX() {
		var consoleStream = new ConsoleStream(new JTextArea());
		LNX lnx = new LNX(consoleStream);
		Assert.assertTrue(lnx.equals(lnx));
	}

	@Override
	public void testBlankNewImage() throws Exception {
	}

	@Override
	public void testImportExportSizes() throws Exception {
	}

	@Override
	public void testImportExportNumFiles() throws Exception {
	}

}
