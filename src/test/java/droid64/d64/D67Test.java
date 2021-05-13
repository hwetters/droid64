package droid64.d64;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class D67Test extends DiskImageBaseTest {

	@Override
	@Test
	public void testToString() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D67(consoleStream).toString().isEmpty());
	}

	@Override
	@Test
	public void testBlankNewImage() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d67");
		D67 d67 = new D67(consoleStream);
		new D67(d67.cbmDisk, consoleStream);
		Assert.assertTrue(d67.equals(d67));
		Assert.assertTrue("Create D67 image ", d67.saveNewImage(imgFile, "D67 UNIT TEST", "00D67"));
		d67.readDirectory();
		d67.readBAM();
		Assert.assertEquals("Free block count ", 670, d67.getBlocksFree());
		Assert.assertEquals("File number max ", 0, d67.getFilesUsedCount());
		Assert.assertEquals("D64 image type ", DiskImageType.D67, d67.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), d67.validate(new ArrayList<ValidationError.Error>()));
		DiskImage img = d67.readImage(imgFile);
		Assert.assertNotNull("Read image ", img);
		Assert.assertEquals("Image type ", DiskImageType.D67, img.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img.cbmDisk.length], consoleStream);
		img.renameImage("NewDisk", "ID");
	}

	@Override
	@Test
	public void testImportExportSizes() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d67");
		D67 img = new D67(consoleStream);
		Assert.assertTrue("Create D64 image ", img.saveNewImage(imgFile, "D67 UNIT TEST", "00D67"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Override
	@Test
	public void testImportExportNumFiles() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d67");
		D67 img = new D67(consoleStream);
		Assert.assertTrue("Create D67 image ", img.saveNewImage(imgFile, "D67 UNIT TEST", "00D67"));
		importExportNumfiles(img, imgFile, D67.FILE_NUMBER_LIMIT, 670);
	}

	@Test(expected=CbmException.class)
	public void testGetFileData_BadFileNumFail() throws CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		new D67(consoleStream).getFileData(Integer.MAX_VALUE);
	}

	@Test(expected=CbmException.class)
	public void testReadPartition_Fail() throws CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		new D67(consoleStream).readPartition(1, 1, 2);
	}

	@Test
	public void testTrackOffsetTable() {
		var consoleStream = new ConsoleStream(new JTextArea());
		D67 img = new D67(consoleStream);
		int secIn = 0;
		int offset = 0;
		for (int trk = img.getFirstTrack(); trk < img.getTrackCount()+img.getFirstTrack(); trk++) {
			CbmTrack cbmTrk = D67Constants.D67_TRACKS[trk];
			int trkSec;
			if (trk >= 1 && trk <= 17) {
				trkSec = 21;
			} else if (trk >= 18 && trk <= 24) {
				trkSec = 20; // D64 has 19 here. That's the only difference between D67 and D64.
			} else if (trk >= 25 && trk <= 30) {
				trkSec = 18;
			} else if (trk >= 31 && trk <= 35) {
				trkSec = 17;
			} else {
				Assert.fail("Illegal track " + trk);
				return;
			}
			Assert.assertEquals("Wrong sector count: " + trk, cbmTrk.getSectors(), trkSec);
			Assert.assertEquals("Wrong offset: " + trk, cbmTrk.getOffset(), offset);
			Assert.assertEquals("Wrong sectorsIn count: " + trk, cbmTrk.getSectorsIn(), secIn);
			secIn += trkSec;
			offset += trkSec * DiskImage.BLOCK_SIZE;
		}
	}
}
