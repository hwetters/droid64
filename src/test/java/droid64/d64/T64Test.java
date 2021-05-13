package droid64.d64;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class T64Test extends DiskImageBaseTest {

	@Override
	@Test
	public void testToString() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new T64(consoleStream).toString().isEmpty());
	}

	@Override
	@Test
	public void testBlankNewImage() throws IOException, CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".t64");
		T64 t64 = new T64(consoleStream);
		Assert.assertTrue(t64.equals(t64));
		Assert.assertTrue("Create T64 image ", t64.saveNewImage(imgFile, "T64 UNIT TEST", "00T64"));
		t64.readDirectory();
		t64.readBAM();
		Assert.assertEquals("Free block count ", 0, t64.getBlocksFree());
		Assert.assertEquals("File number max ", 0, t64.getFilesUsedCount());
		Assert.assertEquals("D64 image type ", DiskImageType.T64, t64.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), t64.validate(new ArrayList<ValidationError.Error>()));
		DiskImage img = t64.readImage(imgFile);
		Assert.assertNotNull("Read image ", img);
		Assert.assertEquals("Image type ", DiskImageType.T64, img.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img.cbmDisk.length], consoleStream);
		img.renameImage("NewDisk", "ID");
	}

	@Override
	@Test
	public void testImportExportSizes() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".t64");
		T64 img = new T64(consoleStream);
		Assert.assertTrue("Create T64 image ", img.saveNewImage(imgFile, "T64 UNIT TEST", "00T64"));
		for (int i = 2; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Override
	@Test
	public void testImportExportNumFiles() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".t64");
		T64 img = new T64(consoleStream);
		Assert.assertTrue("Create T64 image ", img.saveNewImage(imgFile, "T64 UNIT TEST", "00T64"));
		importExportNumfiles(img, imgFile, 100, 0);
	}

	@Override
	protected void importExportFile(DiskImage img, int num, int size, boolean deleteFile) throws CbmException {
		String fileName = "TEST" + Integer.toString(num);
		byte[] data = generateRandom(img instanceof T64 ? size + 2 : size); // T64 handles loadAddr outside of file
		addFileToImage(img, fileName, data);

		// read file
		CbmFile copyFile = img.findFile(fileName, FileType.PRG);
		Assert.assertNotNull("Copy file ", copyFile);
		Assert.assertEquals("Copy file position ", num - 1, copyFile.getDirPosition());
		byte[] copy = img.getFileData(copyFile.getDirPosition());
		Assert.assertNotNull("Copy data ", copy);

		copy[0] = data[0];
		copy[1] = data[1];
		compareData(data, Arrays.copyOfRange(copy, 0, copy.length - 2));

		// delete file
		if (deleteFile) {
			img.deleteFile(copyFile);
			img.readDirectory();
			Assert.assertEquals("File number max ", num - 1, img.getFilesUsedCount());
		}
	}

}
