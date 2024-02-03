package droid64.d64;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class D80Test extends DiskImageBaseTest {

	@Override
	@Test
	public void testToString() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D80(DiskImageType.D80, consoleStream).toString().isEmpty());
	}

	@Override
	@Test
	public void testBlankNewImage() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d80");
		D80 d80 = new D80(DiskImageType.D80, consoleStream);
		new D80(DiskImageType.D80, d80.cbmDisk, consoleStream);
		Assert.assertTrue(d80.equals(d80));
		Assert.assertTrue("Create D80 image ", d80.saveNewImage(imgFile, "D80 UNIT TEST", "00D80"));
		d80.readDirectory();
		d80.readBAM();
		Assert.assertEquals("Free block count ", 2052, d80.getBlocksFree());
		Assert.assertEquals("File number max ", 0, d80.getFilesUsedCount());
		Assert.assertEquals("D80 image type ", DiskImageType.D80, d80.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), d80.validate(new ArrayList<ValidationError.Error>()));
		DiskImage img = d80.readImage(imgFile);
		Assert.assertNotNull("Read image ", img);
		Assert.assertEquals("Image type ", DiskImageType.D80, img.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, img.cbmDisk, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img.cbmDisk.length], consoleStream);
		img.renameImage("NewDisk", "ID");
	}

	@Override
	@Test
	public void testImportExportSizes() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d80");
		D80 img = new D80(DiskImageType.D80, consoleStream);
		Assert.assertTrue("Create D80 image ", img.saveNewImage(imgFile, "D80 UNIT TEST", "00D80"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Override
	@Test
	public void testImportExportNumFiles() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d80");
		D80 img = new D80(DiskImageType.D80, consoleStream);
		Assert.assertTrue("Create D80 image ", img.saveNewImage(imgFile, "D80 UNIT TEST", "00D80"));
		importExportNumfiles(img, imgFile, D80.FILE_NUMBER_LIMIT, 2052);
	}

	@Test
	public void testTrackOffsetTable() {
		var consoleStream = new ConsoleStream(new JTextArea());
		D80 img = new D80(DiskImageType.D80, consoleStream);
		int secIn = 0;
		int offset = 0;
		for (int trk = img.getFirstTrack(); trk < img.getTrackCount()+img.getFirstTrack(); trk++) {
			CbmTrack cbmTrk = D80Constants.D80_TRACKS[trk];
			int trkSec;
			if (trk >= 1 && trk <= 39) {
				trkSec = 29;
			} else if (trk >= 40 && trk <= 53) {
				trkSec = 27;
			} else if (trk >= 54 && trk <= 64) {
				trkSec = 25;
			} else if (trk >= 65 && trk <= 77) {
				trkSec = 23;
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
