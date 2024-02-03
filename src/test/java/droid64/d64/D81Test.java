package droid64.d64;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.IntStream;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

public class D81Test extends DiskImageBaseTest {

	/** It is 3160 free blocks on a blank formatted D81 image */
	private static final int BLOCKS_FREE = 3160;

	@Override
	@Test
	public void testToString() {
		var consoleStream = new ConsoleStream(new JTextArea());
		Assert.assertFalse(new D81(DiskImageType.D81, consoleStream).toString().isEmpty());
	}

	@Override
	@Test
	public void testBlankNewImage() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d81", true);
		D81 d81 = new D81(DiskImageType.D81, consoleStream);
		new D81(DiskImageType.D81, d81.cbmDisk, consoleStream);
		Assert.assertTrue(d81.equals(d81));
		Assert.assertTrue("Create D81 image ", d81.saveNewImage(imgFile, "D81 BLANK", "00D81"));
		d81.readDirectory();
		d81.readBAM();
		Assert.assertEquals("Free block count ", BLOCKS_FREE, d81.getBlocksFree());
		Assert.assertEquals("File number max ", 0, d81.getFilesUsedCount());
		Assert.assertEquals("D64 image type ", DiskImageType.D81, d81.getImageFormat());
		Assert.assertEquals("Validation errors ", Integer.valueOf(0), d81.validate(new ArrayList<ValidationError.Error>()));
		DiskImage img = d81.readImage(imgFile);
		Assert.assertNotNull("Read image ", img);
		Assert.assertEquals("Image type ", DiskImageType.D81, img.getImageFormat());
		DiskImage.getDiskImage(imgFile, consoleStream);
		DiskImage.getDiskImage(imgFile, new byte[img.cbmDisk.length], consoleStream);
		img.renameImage("NEW NAME", "ID");
	}

	@Override
	@Test
	public void testImportExportSizes() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d81", true);
		D81 img = new D81(DiskImageType.D81, consoleStream);
		Assert.assertTrue("Create D81 image ", img.saveNewImage(imgFile, "D81 SIZES", "00D81"));
		for (int i = 0; i < TEST_FILE_SIZE_MAX; i++) {
			importExportFile(img, 1, i, true);
		}
	}

	@Override
	@Test
	public void testImportExportNumFiles() throws Exception {
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d81", true);
		D81 img = new D81(DiskImageType.D81, consoleStream);
		Assert.assertTrue("Create D81 image ", img.saveNewImage(imgFile, "D81 NUM FILES", "00D81"));
		importExportNumfiles(img, imgFile, D81.FILE_NUMBER_LIMIT, BLOCKS_FREE);
	}

	@Test
	public void testMkdir() throws Exception {
		// prepare image file
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d81", true);
		D81 d81 = new D81(DiskImageType.D81, consoleStream);
		Assert.assertTrue("Create D81 image ", d81.saveNewImage(imgFile, "D81 MKDIR TEST", "00D81"));
		d81.readDirectory();
		d81.readBAM();
		Assert.assertEquals("Free blocks initially", BLOCKS_FREE, d81.getBlocksFree());

		// P1: tracks 1-3 (request 0 blocks which should return 3*40 blocks)
		mkPart("P1", 0, 1, 0*40, BLOCKS_FREE - 3*40, d81, imgFile);
		// P2: tracks 4-7 (should be created just above the previous)
		mkPart("P2", 1, 4, 4*40, BLOCKS_FREE - 3*40 - 4*40, d81, imgFile);
		// P3: tracks 8-37 (take all but 2 tracks below BAM track)
		mkPart("P3", 2, 8, 30*40, BLOCKS_FREE - 3*40 - 4*40 - 30*40, d81, imgFile);
		// P4: tracks 41-43 (should be created just above BAM track)
		mkPart("P4", 3, 41, 3*40, BLOCKS_FREE - 3*40 - 4*40 - 30*40 - 3*40, d81, imgFile);

		// P5 (expects failure due to not enough space)
		try {
			d81.makedir("P5", 38*40, "05");
			Assert.fail("Shouldn't be possible to create larger partition than available space");
		} catch (CbmException e) {};
		d81.readDirectory();
		d81.readBAM();
		Assert.assertEquals("Free blocks after P5", BLOCKS_FREE - 3*40 - 4*40 - 30*40 - 3*40, d81.getBlocksFree());

		// P6: tracks 44-80 (all remaining free tracks)
		mkPart("P6", 4, 44, 37*40, BLOCKS_FREE - 3*40 - 4*40 - 30*40 - 3*40 - 37*40, d81, imgFile);

		// Now only track 38 and 39 are free. Need at least 2 tracks for a partition.
	}

	@Test
	public void testCopyToPartition() throws Exception {

		// prepare image file
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d81", true);
		D81 d81 = new D81(DiskImageType.D81, consoleStream);
		Assert.assertTrue("Create D81 image ", d81.saveNewImage(imgFile, "D81 COPY PART", "00D81"));
		d81.readDirectory();
		d81.readBAM();
		Assert.assertEquals("Free blocks initially", BLOCKS_FREE, d81.getBlocksFree());
		Assert.assertEquals("Expect no files before", 0, d81.filesUsedCount);

		// P1: tracks 1-3 (request 0 blocks which should return 3*40 blocks)
		CbmFile p1 = mkPart("P1", 0, 1, 0*40, BLOCKS_FREE - 3*40, d81, imgFile);
		Assert.assertNull(d81.setCurrentPartition(null));
		Assert.assertNotNull(p1);
		Assert.assertEquals("Track of P1 partition", 1, p1.getTrack());

		d81.readDirectory();
		Assert.assertEquals("Expect 1 file (P1 partition)", 1, d81.filesUsedCount);
		Assert.assertEquals("Name of P1 partition", "P1", d81.getCbmFile(0).getName());
		Assert.assertEquals("Track of P1 partition", 1, d81.getCbmFile(0).getTrack());
		Assert.assertEquals("Block count of P1 partition", 3*40, d81.getCbmFile(0).getSizeInBlocks());

		// PARTFILE_1.1

		CbmFile cf = d81.setCurrentPartition(1);
		Assert.assertNotNull("Failed to set partition: ", cf);
		Assert.assertNotNull(d81.getPartitionFile(1));
		Assert.assertNotNull(d81.setCurrentPartition(1));

		byte[] data_1_1 = generateRandom(400);
		CbmFile pf1 = addFileToImage(d81, "PARTFILE 1.1", data_1_1);
		Assert.assertNotNull("Write file 1.1: ",pf1);
		Assert.assertTrue("Write D81 image", d81.saveAs(imgFile));
		Assert.assertEquals("Partfile 1.1", 1, d81.filesUsedCount);

		Assert.assertNull(d81.setCurrentPartition(null));
		d81.readDirectory();
		Assert.assertEquals("Expect 1 file (P1 partition)", 1, d81.filesUsedCount);
		Assert.assertEquals("Name of P1 partition", "P1", d81.getCbmFile(0).getName());

		Assert.assertNotNull(d81.setCurrentPartition(1));
		d81.readDirectory();
		Assert.assertEquals("Expect 1 file (PARTFILE 1.1)", 1, d81.filesUsedCount);
		Assert.assertEquals("Name of file in P1 ", "PARTFILE 1.1", d81.getCbmFile(0).getName());

		Assert.assertTrue("Read file data ", compareData(data_1_1, d81.getFileData(0)));

		// P2: track 4-8
		Assert.assertNull(d81.setCurrentPartition(null));
		d81.readBAM();
		CbmFile p2 = mkPart("P2", 1, 4, 5*40, BLOCKS_FREE - 3*40 - 5*40, d81, imgFile);
		Assert.assertNotNull(p2);
		Assert.assertEquals("Track of P2 partition", 4, p2.getTrack());

		Assert.assertNull(d81.setCurrentPartition(null));
		d81.readDirectory();
		Assert.assertEquals("Expect 2 file2 (P1 & P2 partitions)", 2, d81.filesUsedCount);
		Assert.assertEquals("Name of P1 partition", "P1", d81.getCbmFile(0).getName());
		Assert.assertEquals("Name of P2 partition", "P2", d81.getCbmFile(1).getName());
		Assert.assertEquals("Block count of P2 partition", 5*40, d81.getCbmFile(1).getSizeInBlocks());
		Assert.assertEquals("Track of P2 partition", 4, d81.getCbmFile(1).getTrack());

		Assert.assertNotNull(d81.setCurrentPartition(4));
	}

	@Test
	public void testFillPartitionWithFiles() throws Exception {
		// prepare image file
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d81", true);
		D81 d81 = new D81(DiskImageType.D81, consoleStream);
		Assert.assertTrue("Create D81 image ", d81.saveNewImage(imgFile, "D81 PARTFILES", "00D81"));
		d81.markSectorUsed(1, 1); // mark a block on track as used
		d81.readDirectory();
		d81.readBAM();
		Assert.assertEquals("Free blocks initially", BLOCKS_FREE - 1, d81.getBlocksFree());
		Assert.assertEquals("Expect no files before", 0, d81.filesUsedCount);
		// create partition
		mkPart("P1", 0, 2, 10*40, BLOCKS_FREE - 1 - 10*40, d81, imgFile);
		Assert.assertNotNull("Set partition", d81.setCurrentPartition(2));
		// add max number of files to partition
		for (int i=0; i < D81.FILE_NUMBER_LIMIT; i++) {
			CbmFile cf = addFileToImage(d81, String.format("PARTFILE_%03d",i), generateRandom(200));
			Assert.assertNotNull("Write file "+i, cf);
			Assert.assertTrue("Write D81 image", d81.saveAs(imgFile));
		}
		// try to add one file more than max number of files
		CbmFile cbmFile = new CbmFile();
		cbmFile.setName("PARTFILE_FAIL");
		cbmFile.setFileType(FileType.PRG);
		cbmFile.setLoadAddr(0);
		Assert.assertFalse("Could add too many files", d81.saveFile(cbmFile, false, generateRandom(200)));
	}

	@Test
	public void testIsTrackInCurrentPartition() throws Exception {
		// prepare image file
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d81", true);
		D81 d81 = new D81(DiskImageType.D81, consoleStream);
		Assert.assertTrue("Create D81 image ", d81.saveNewImage(imgFile, "D81 TRK PART", "00D81"));
		d81.markSectorUsed(1, 1); // mark a block on track as used
		d81.readDirectory();
		d81.readBAM();
		Assert.assertEquals("Free blocks initially", BLOCKS_FREE - 1, d81.getBlocksFree());
		Assert.assertEquals("Expect no files before", 0, d81.filesUsedCount);

		// P1: tracks 2-6 (request 200 blocks)
		mkPart("P1", 0, 2, 200, BLOCKS_FREE - 1 - 5*40, d81, imgFile);

		d81.setCurrentPartition(null);
		IntStream.range(1, 80).forEach(trk -> Assert.assertTrue("No partition set - " + trk, d81.isTrackInCurrentPartition(trk)));

		Assert.assertNotNull("Set partition", d81.setCurrentPartition(2));

		Assert.assertFalse("Before partition", d81.isTrackInCurrentPartition(1));
		IntStream.range(2, 6).forEach(trk -> Assert.assertTrue("Partition set - " + trk, d81.isTrackInCurrentPartition(trk)));
		IntStream.range(7, 80).forEach(trk -> Assert.assertFalse("After partition - " + trk, d81.isTrackInCurrentPartition(trk)));
	}

	@Test
	public void testCreateAndDeletePartition() throws Exception {

		// prepare image file
		var consoleStream = new ConsoleStream(new JTextArea());
		File imgFile = getTempFile(".d81", true);
		D81 d81 = new D81(DiskImageType.D81, consoleStream);
		Assert.assertTrue("Create D81 image ", d81.saveNewImage(imgFile, "D81 DEL PART", "00D81"));
		d81.readDirectory();
		d81.readBAM();
		Assert.assertEquals("Free blocks initially", BLOCKS_FREE, d81.getBlocksFree());
		Assert.assertEquals("Expect no files before", 0, d81.filesUsedCount);

		// P1: tracks 1-5 (request 200 blocks)1
		mkPart("P1", 0, 1, 200, BLOCKS_FREE - 5*40, d81, imgFile);
		d81.readDirectory();
		Assert.assertEquals("Expect one CBM file", 1, d81.filesUsedCount);


		// read file
		CbmFile partFile = d81.findFile("P1", FileType.CBM);
		Assert.assertNotNull("P1 CBM file ", partFile);
		Assert.assertEquals("Partition size", 200, partFile.getSizeInBlocks());

		d81.deleteFile(partFile);
		Assert.assertTrue("write image ", d81.saveAs(imgFile));

		d81.readDirectory();
		d81.readBAM();
		Assert.assertEquals("Free blocks after delete", BLOCKS_FREE, d81.getBlocksFree());
		Assert.assertEquals("Expect no files after", 0, d81.filesUsedCount);
	}

	private CbmFile mkPart(final String pName, final int fileNum, final int track, final int size, final int free, D81 d81, File imgFile) throws CbmException {
		int partSize = Math.max(3*40, size);

		Assert.assertEquals(pName+": Free blocks before ", free + partSize, d81.getBlocksFree());
		CbmFile pf = d81.makedir(pName, size, String.format("%02d", fileNum));
		Assert.assertNotNull("makedir failed: ", pf);
		Assert.assertTrue(pName+": write image ", d81.saveAs(imgFile));
		d81.readDirectory();
		d81.readBAM();

		CbmFile cf = d81.getCbmFile(fileNum);
		Assert.assertNotNull(pName+":entry", cf);
		Assert.assertEquals(pName+":name", pName, cf.getName());
		Assert.assertEquals(pName+":type", FileType.CBM, cf.getFileType());
		Assert.assertEquals(pName+":size", partSize, cf.getSizeInBlocks());
		Assert.assertEquals(pName+":sector", 0, cf.getSector());
		Assert.assertEquals(pName+":track", track, cf.getTrack());
		Assert.assertEquals(pName+": Free blocks after", free, d81.getBlocksFree());

		d81.readPartition(track, 0, partSize);
		d81.readBAM();
		d81.setCurrentPartition(null);
		return pf;
	}
}
