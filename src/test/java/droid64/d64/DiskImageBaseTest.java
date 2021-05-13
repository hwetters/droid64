package droid64.d64;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.Assert;

import droid64.gui.Setting;

public abstract class DiskImageBaseTest {

	private static final Random RANDOM = new Random();

	/**
	 * The maximum file size used when testing writing a file of different sizes and it can
	 * be written and read with same contents. Should be a value between 0 and 65535.
	 */
	protected static final int TEST_FILE_SIZE_MAX = 413;
	/** The size of each file when creating maximum number of files on an image. */
	protected static final int TEST_NUM_FILES_SIZE = 100;

	public abstract void testBlankNewImage() throws Exception;
	public abstract void testImportExportSizes() throws Exception;
	public abstract void testImportExportNumFiles() throws Exception;
	public abstract void testToString() throws Exception;

	public DiskImageBaseTest() {
		try {
			Setting.load(new File("src/test/resources/droid64/gui/test.config"));
		} catch (IOException ignore) {
		}
	}

	protected CbmFile addFileToImage(DiskImage img, String fileName, byte[] data) {
		// init
		img.readDirectory();
		img.readBAM();
		int orgFileNum = img.getFilesUsedCount();
		// create file
		CbmFile cbmFile = new CbmFile();
		cbmFile.setName(fileName);
		cbmFile.setFileType(FileType.PRG);
		cbmFile.setLoadAddr(0);
		// save file
		Assert.assertTrue("saveFile failed: ", img.saveFile(cbmFile, false, data));
		img.readDirectory();
		Assert.assertEquals("Files used ", orgFileNum + 1, img.getFilesUsedCount());
		return cbmFile;
	}

	protected byte[] generateRandom(int size) {
		byte[] data = new byte[size];
		RANDOM.nextBytes(data);
		return data;
	}

	protected boolean compareData(byte[] d1, byte[] d2) {
		if (d1 == d2) {
			return true;
		} else if (d1 == null || d2 == null) {
			Assert.fail("compareData: one is null");
			return false;
		} else if (d1.length != d2.length) {
			Assert.assertEquals("compareData: lengths are different", d1.length, d2.length);
			return false;
		} else {
			for (int i = 0; i < d1.length; i++) {
				if (d1[i] != d2[i]) {
					Assert.fail("compareData: bytes differ at pos "+i +"\nd1=\n"+Utility.hexDump(d1)+"\nd2=\n"+Utility.hexDump(d2));
					return false;
				}
			}
			return true;
		}
	}

	protected File getTempFile(String suffix) throws IOException {
		return getTempFile(suffix, true);
	}

	protected File getTempFile(String suffix, boolean deleteOnExit) throws IOException {
		File tmpFile = File.createTempFile("UnitTest_", suffix);
		if (deleteOnExit) {
			tmpFile.deleteOnExit();
		}
		return tmpFile;
	}

	protected void deleteAllFiles(DiskImage img) throws CbmException {
		for (int i = 0; i < img.getFileCapacity(); i++) {
			CbmFile cf = img.getCbmFile(i);
			img.deleteFile(cf);
		}
	}

	protected void importExportNumfiles(DiskImage img, File imgFile, int fileNumberLimit, int maxFreeBlocks)
			throws CbmException {
		for (int i = 0; i < fileNumberLimit; i++) {
			importExportFile(img, i + 1, TEST_NUM_FILES_SIZE, false);
		}
		img.saveAs(imgFile);
		Assert.assertEquals("File number max ", fileNumberLimit, img.getFilesUsedCount());
		// delete all files and check it is empty again.
		deleteAllFiles(img);
		img.saveAs(imgFile);
		img.readDirectory();
		img.readBAM();
		Assert.assertEquals("Free block count ", maxFreeBlocks, img.getBlocksFree());
		Assert.assertEquals("File number max ", 0, img.getFilesUsedCount());
	}

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

		compareData(data, copy);

		// delete file
		if (deleteFile) {
			img.deleteFile(copyFile);
			img.readDirectory();
			Assert.assertEquals("File number max ", num - 1, img.getFilesUsedCount());
		}
	}

}
