package droid64.d64;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class D71Test extends DiskImageBaseTest {

	@Override
	@Test
	public void testToString() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D71(DiskImageType.D71, consoleStream).toString().isEmpty());
	}

	@Override
	@Test
	public void testBlankNewImage() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d71");
		D71 d71 = new D71(DiskImageType.D71, consoleStream);
		new D71(DiskImageType.D71, d71.cbmDisk, consoleStream);
		Assert.assertTrue(d71.equals(d71));
		Assert.assertTrue("Create D71 image ", d71.saveNewImage(imgFile, "D71 UNIT TEST", "00D71"));
		d71.readDirectory();
		d71.readBAM();
		Assert.assertEquals("Free block count ", 1328, d71.getBlocksFree());
		Assert.assertEquals("File number max ", 0, d71.getFilesUsedCount());
		Assert.assertEquals("D64 image type ", DiskImageType.D71, d71.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), d71.validate(new ArrayList<ValidationError.Error>()));
		DiskImage img = d71.readImage(imgFile);
		Assert.assertNotNull("Read image ", img);
		Assert.assertEquals("Image type ", DiskImageType.D71, img.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img.cbmDisk.length], consoleStream);
		img.renameImage("NewDisk", "ID");
	}

	@Override
	@Test
	public void testImportExportSizes() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d71");
		D71 img = new D71(DiskImageType.D71, consoleStream);
		Assert.assertTrue("Create D71 image ", img.saveNewImage(imgFile, "D71 UNIT TEST", "00D71"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Override
	@Test
	public void testImportExportNumFiles() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d71");
		D71 img = new D71(DiskImageType.D71, consoleStream);
		Assert.assertTrue("Create D71 image ", img.saveNewImage(imgFile, "D71 UNIT TEST", "00D71"));
		importExportNumfiles(img, imgFile, D71.FILE_NUMBER_LIMIT, 1328);
	}

	@Test(expected=CbmException.class)
	public void testGetFileData_BadFileNumFail() throws CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		new D71(DiskImageType.D71, consoleStream).getFileData(Integer.MAX_VALUE);
	}

	@Test(expected=CbmException.class)
	public void testReadPartition_Fail() throws CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		new D71(DiskImageType.D71, consoleStream).readPartition(1, 1, 2);
	}

	@Test
	public void testTrackOffsetTable() {
		var consoleStream = new ConsoleStream(new JTextArea());
		D71 img = new D71(DiskImageType.D71, consoleStream);
		int secIn = 0;
		int offset = 0;
		for (int trk = img.getFirstTrack(); trk < img.getTrackCount()+img.getFirstTrack(); trk++) {
			CbmTrack cbmTrk = D71Constants.D71_TRACKS[trk];
			int trkSec;
			if (trk >= 1 && trk <= 17 || trk >= 36 && trk <= 52) {
				trkSec = 21;
			} else if (trk >= 18 && trk <= 24 || trk >= 53 && trk <= 59) {
				trkSec = 19;
			} else if (trk >= 25 && trk <= 30 || trk >= 60 && trk <= 65) {
				trkSec = 18;
			} else if (trk >= 31 && trk <= 35 || trk >= 66 && trk <= 70) {
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
