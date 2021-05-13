package droid64.d64;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class D64Test extends DiskImageBaseTest {

	@Override
	@Test
	public void testToString() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D64(consoleStream).toString().isEmpty());
	}

	@Test
	public void test() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D64(consoleStream).isCpmImage());
	}

	@Override
	@Test
	public void testBlankNewImage() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d64");
		D64 d64 = new D64(consoleStream);
		new D64(d64.cbmDisk, consoleStream);
		Assert.assertTrue(d64.equals(d64));
		Assert.assertTrue("Create D64 image ", d64.saveNewImage(imgFile, "D64 UNIT TEST", "00D64"));
		d64.readDirectory();
		d64.readBAM();
		Assert.assertEquals("Free block count ", 664, d64.getBlocksFree());
		Assert.assertEquals("File number max ", 0, d64.getFilesUsedCount());
		Assert.assertEquals("D64 image type ", DiskImageType.D64, d64.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), d64.validate(new ArrayList<ValidationError.Error>()));
		DiskImage img = d64.readImage(imgFile);
		Assert.assertNotNull("Read image ", img);
		Assert.assertEquals("Image type ", DiskImageType.D64, img.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img.cbmDisk.length], consoleStream);
		img.renameImage("NewDisk", "ID");
	}

	@Test
	public void testGetDisk() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		D64 d64 = new D64(consoleStream);
		File imgFile = getTempFile(".d64");
		Assert.assertTrue("Create D64 image ", d64.saveNewImage(imgFile, "D64 UNIT TEST", "00D64"));
		d64.addDirectoryEntry(new CbmFile(), 1, 1, false, 11);

		d64.renameFile(0, "newName", FileType.PRG);
		Assert.assertNotNull(d64.getDisk());
		Assert.assertNotNull(d64.getBlock(18, 1));
	}

	@Test
	public void testSwitchFileLocations() throws Exception {
		File imgFile = getTempFile(".d64");
		var consoleStream = new ConsoleStream(new JTextArea());
		D64 img = new D64(consoleStream);
		Assert.assertTrue("Create D64 image ", img.saveNewImage(imgFile, "D64 UNIT TEST", "00D64"));
		CbmFile cf1 = addFileToImage(img, "file1", new byte[10]);
		CbmFile cf2 = addFileToImage(img, "file2", new byte[10]);
		img.switchFileLocations(cf1, cf2);
	}

	@Override
	@Test
	public void testImportExportNumFiles() throws Exception {
		File imgFile = getTempFile(".d64");
		var consoleStream = new ConsoleStream(new JTextArea());
		D64 img = new D64(consoleStream);
		Assert.assertTrue("Create D64 image ", img.saveNewImage(imgFile, "D64 UNIT TEST", "00D64"));
		importExportNumfiles(img, imgFile, D64.FILE_NUMBER_LIMIT, 664);
	}

	@Override
	@Test
	public void testImportExportSizes() throws Exception {
		File imgFile = getTempFile(".d64");
		var consoleStream = new ConsoleStream(new JTextArea());
		D64 img = new D64(consoleStream);
		Assert.assertTrue("Create D64 image ", img.saveNewImage(imgFile, "D64 UNIT TEST", "00D64"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Test(expected=CbmException.class)
	public void testGetFileData_BadFileNumFail() throws CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		new D64(consoleStream).getFileData(Integer.MAX_VALUE);
	}

	@Test(expected=CbmException.class)
	public void testReadPartition_Fail() throws CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		new D64(consoleStream).readPartition(1, 1, 2);
	}

	@Test
	public void testGetSectorFromOffset() {
		var consoleStream = new ConsoleStream(new JTextArea());
		D64 d64 = new D64(consoleStream);
		int t=-1;
		StringBuilder buf = new StringBuilder();
		for (int b = 0; b<683; b++) {
			TrackSector ts = d64.getSector(b*256);
			if (t!=ts.getTrack() && b>0) {
				if(t!=-1) {
					buf.append('\n');
				}
				t=ts.track;
			}
			buf.append(String.format(" %3d=%2d/%-3d",b,ts.track,ts.sector));
		}
		Assert.assertFalse("sector from offset buffer", buf.toString().isEmpty());
	}


	@Test
	public void testTorture() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		D64 img = new D64(consoleStream);

		Random fileRandom = new Random(img.getClass().getName().hashCode());

		File imgFile = getTempFile(".d64");
		Assert.assertTrue("Save image ", img.saveNewImage(imgFile, "D64 UNIT TEST", "00D64"));

		for (int i = 0; i < D64.FILE_NUMBER_LIMIT; i++) {
			img.readDirectory();
			img.readBAM();
			int size = Math.abs(fileRandom.nextInt()) % 8192; // random file size. even if all files are large, there should be a couple of them

			if (img.getBlocksFree()>(size/254)+1) {
				importExportFile(img, i + 1, size, false);
			} else {
				break;
			}
		}
		img.saveAs(imgFile);
		img.readDirectory();
		img.readBAM();

		Assert.assertTrue("no files", img.getFilesUsedCount() > 0);
		Assert.assertTrue("too many files", img.getFilesUsedCount() < D64.FILE_NUMBER_LIMIT );
		Assert.assertTrue("too many free blocks", img.getBlocksFree() < 664);
	}

	@Test
	public void testTrackOffsetTable() {
		var consoleStream = new ConsoleStream(new JTextArea());
		D64 img = new D64(consoleStream);
		int secIn = 0;
		int offset = 0;
		for (int trk = img.getFirstTrack(); trk < img.getTrackCount()+img.getFirstTrack(); trk++) {
			CbmTrack cbmTrk = D64Constants.D64_TRACKS[trk];
			int trkSec;
			if (trk >= 1 && trk <= 17) {
				trkSec = 21;
			} else if (trk >= 18 && trk <= 24) {
				trkSec = 19;
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
