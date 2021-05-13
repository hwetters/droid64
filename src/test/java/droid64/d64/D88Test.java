package droid64.d64;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class D88Test extends DiskImageBaseTest {

	@Test
	public void testGetSectorFromOffset() {
		var consoleStream = new ConsoleStream(new JTextArea());
		D88 d88 = new D88(consoleStream);
		StringBuilder buf = new StringBuilder();
		for (int t=0;t<77;t++) {
			buf.append(t).append('\t');
			for (int s=0;s<52;s++) {
				buf.append(""+d88.getSector((t*52+s)*256));
			}
			buf.append('\n');
		}
		Assert.assertFalse("sectoroffset", buf.toString().isEmpty());
	}

	@Override
	@Test
	public void testBlankNewImage() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d88");
		D88 d88 = new D88(consoleStream);
		new D88(d88.cbmDisk, consoleStream);
		Assert.assertTrue(d88.equals(d88));
		Assert.assertTrue("Create D88 image ", d88.saveNewImage(imgFile, "D88 UNIT TEST", "00D88"));
		d88.readDirectory();
		d88.readBAM();
		Assert.assertEquals("Free block count ", 3946, d88.getBlocksFree());
		Assert.assertEquals("File number max ", 0, d88.getFilesUsedCount());
		Assert.assertEquals("D88 image type ", DiskImageType.D88, d88.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), d88.validate(new ArrayList<>()));
		DiskImage img = d88.readImage(imgFile);
		Assert.assertNotNull("Read image ", img);
		Assert.assertEquals("Image type ", DiskImageType.D88, img.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, img.cbmDisk, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img.cbmDisk.length], consoleStream);
		img.renameImage("NewDisk", "ID");
		Assert.assertEquals("BAM track count", 77, img.getBamTable().length);
		img.writeDirectoryEntry(new CbmFile(), 1);
	}

	@Override
	@Test
	public void testImportExportSizes() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d88");
		D88 img = new D88(consoleStream);
		Assert.assertTrue("Create D88 image ", img.saveNewImage(imgFile, "D88 UNIT TEST", "00D88"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Override
	@Test
	public void testImportExportNumFiles() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d88");
		D88 img = new D88(consoleStream);
		Assert.assertTrue("Create D88 image ", img.saveNewImage(imgFile, "D88 UNIT TEST", "00D88"));
		importExportNumfiles(img, imgFile, D88.FILE_NUMBER_LIMIT, 3946 - 50); // On D88 the directory blocks counts too
	}

	@Override
	@Test
	public void testToString() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D88(consoleStream).toString().isEmpty());
		Assert.assertEquals("10", new D88(consoleStream).getSectorTitle(10));
		Assert.assertEquals("33", new D88(consoleStream).getSectorTitle(27));
	}

}
