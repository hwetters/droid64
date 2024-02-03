package droid64.d64;

import java.util.ArrayList;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;


public class D90Test extends DiskImageBaseTest {

	@Override
	public void testBlankNewImage() throws Exception {		
		// split into two separate
	}
	@Override
	public void testImportExportNumFiles() throws Exception {
		// split into two separate
	}
	
	@Override
	public void testImportExportSizes() throws Exception {
		// split into two separate
	}
	
	@Test	
	public void testBlankNewImageD9060() throws Exception {
		// Track 0 reserved for ConfigSector and BadBlockList
		// Tracks*Sectors*Heads - Sectors*Heads - DirSector - BamSectors - HeaderSector
		testBlank(DiskImageType.D90_9060, D90.D9060_SIZE, 153*32*4 - 32*4 - 1 - 20 -1);
	}
	
	@Test
	public void testBlankNewImageD9090() throws Exception {		
		// Track 0 reserved for ConfigSector and BadBlockList
		// Tracks*Sectors*Heads - Sectors*Heads - DirSector - BamSectors - HeaderSector
		testBlank(DiskImageType.D90_9090, D90.D9090_SIZE, 153*32*6 - 32*6 - 1 - 20 -1);
	}
	
	@Test
	public void testImportExportSizesd9060() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		var imgFile = getTempFile(".d90");
		var img = new D90(DiskImageType.D90_9060, consoleStream);
		Assert.assertTrue("Create D90 image ", img.saveNewImage(imgFile, "D9060 UNIT TEST", "D9060"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Test
	public void testImportExportSizesd9090() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		var imgFile = getTempFile(".d90");
		var img = new D90(DiskImageType.D90_9090, consoleStream);
		Assert.assertTrue("Create D90 image ", img.saveNewImage(imgFile, "D9090 UNIT TEST", "D9090"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}
	
	@Test
	public void testImportExportNumFilesD9060() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		var imgFile1 = getTempFile(".d90");
		var img1 = new D90(DiskImageType.D90_9060, consoleStream);
		Assert.assertTrue("Create D90 image ", img1.saveNewImage(imgFile1, "D90 UNIT TEST", "D9060"));
		importExportNumfiles(img1, imgFile1, D90.D9060_FILE_NUMBER_LIMIT, D90.D9060_FILE_NUMBER_LIMIT);
	}

	@Test
	public void testImportExportNumFilesD9090() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		var imgFile = getTempFile(".d90");
		var img = new D90(DiskImageType.D90_9090, consoleStream);
		img.setFile(imgFile);
		img.setCompressed(false);
		Assert.assertTrue("Create D90 image ", img.saveNewImage(imgFile, "D90 UNIT TEST", "D9090"));
		importExportNumfiles(img, imgFile, D90.D9090_FILE_NUMBER_LIMIT, D90.D9090_FILE_NUMBER_LIMIT);
	}
	
	@Override
	@Test
	public void testToString() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D90(DiskImageType.D90_9060, consoleStream).toString().isEmpty());
		Assert.assertFalse(new D90(DiskImageType.D90_9090, consoleStream).toString().isEmpty());
	}
	
	private void testBlank(DiskImageType imgType, int imgSize, int freeBlocks)  throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());

		var imgFile = getTempFile(".d90");
		
		var img1 = new D90(imgType, consoleStream);
		img1.cbmDisk = new byte[imgSize];
		new D90(imgType, img1.cbmDisk, consoleStream);
		Assert.assertTrue("Create D90 image ", img1.saveNewImage(imgFile, "D90 UNIT TEST", "D90"));
		img1.readDirectory();
		img1.readBAM();
		Assert.assertEquals("Free block count ", freeBlocks, img1.getBlocksFree());
		Assert.assertEquals("File number max ", 0, img1.getFilesUsedCount());
		Assert.assertEquals("D90 image type ", imgType, img1.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), img1.validate(new ArrayList<ValidationError.Error>()));

		var img2 = img1.readImage(imgFile);
		Assert.assertNotNull("Read image ", img2);
		Assert.assertEquals("Image type ", imgType, img2.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img2.cbmDisk.length], consoleStream);
		img2.renameImage("NewDisk", "ID");
	}
}