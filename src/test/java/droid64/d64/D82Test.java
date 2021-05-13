package droid64.d64;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class D82Test extends DiskImageBaseTest {

	@Override
	@Test
	public void testToString() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D82(consoleStream).toString().isEmpty());
	}

	@Override
	@Test
	public void testBlankNewImage() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d82");
		D82 d82 = new D82(consoleStream);
		new D82(d82.cbmDisk, consoleStream);
		Assert.assertTrue(d82.equals(d82));
		Assert.assertTrue("Create D82 image ", d82.saveNewImage(imgFile, "D82 UNIT TEST", "00D82"));
		d82.readDirectory();
		d82.readBAM();
		Assert.assertEquals("Free block count ", 4133, d82.getBlocksFree());
		Assert.assertEquals("File number max ", 0, d82.getFilesUsedCount());
		Assert.assertEquals("D64 image type ", DiskImageType.D82, d82.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), d82.validate(new ArrayList<ValidationError.Error>()));
		DiskImage img = d82.readImage(imgFile);
		Assert.assertNotNull("Read image ", img);
		Assert.assertEquals("Image type ", DiskImageType.D82, img.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img.cbmDisk.length], consoleStream);
		img.renameImage("NewDisk", "ID");
	}

	@Override
	@Test
	public void testImportExportSizes() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d82");
		D82 img = new D82(consoleStream);
		Assert.assertTrue("Create D82 image ", img.saveNewImage(imgFile, "D82 UNIT TEST", "00D82"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Override
	@Test
	public void testImportExportNumFiles() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d82");
		D82 img = new D82(consoleStream);
		Assert.assertTrue("Create D82 image ", img.saveNewImage(imgFile, "D82 UNIT TEST", "00D82"));
		importExportNumfiles(img, imgFile, D82.FILE_NUMBER_LIMIT, 4133);
	}

	@Test
	public void testTrackOffsetTable() {
		var consoleStream = new ConsoleStream(new JTextArea());
		D82 img = new D82(consoleStream);
		int secIn = 0;
		int offset = 0;
		for (int trk = img.getFirstTrack(); trk < img.getTrackCount()+img.getFirstTrack(); trk++) {
			CbmTrack cbmTrk = D82Constants.D82_TRACKS[trk];
			int trkSec;
			if (trk >= 1 && trk <= 39 || trk >= 78 && trk <= 116) {
				trkSec = 29;
			} else if (trk >= 40 && trk <= 53 || trk >= 117 && trk <= 130) {
				trkSec = 27;
			} else if (trk >= 54 && trk <= 64 || trk >= 131 && trk <= 141) {
				trkSec = 25;
			} else if (trk >= 65 && trk <= 77 || trk >= 142 && trk <= 154) {
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
