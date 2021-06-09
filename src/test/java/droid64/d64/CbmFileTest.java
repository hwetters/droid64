package droid64.d64;

import org.junit.Assert;
import org.junit.Test;

public class CbmFileTest {

	@Test
	public void testGetFileTypeFromFileExtension() {
		Assert.assertEquals(FileType.DEL, CbmFile.getFileTypeFromFileExtension(".del"));
		Assert.assertEquals(FileType.PRG, CbmFile.getFileTypeFromFileExtension(".prg"));
		Assert.assertEquals(FileType.SEQ, CbmFile.getFileTypeFromFileExtension(".seq"));
		Assert.assertEquals(FileType.REL, CbmFile.getFileTypeFromFileExtension(".rel"));
		Assert.assertEquals(FileType.USR, CbmFile.getFileTypeFromFileExtension(".usr"));
		Assert.assertEquals(FileType.PRG, CbmFile.getFileTypeFromFileExtension(".xxx"));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void test() {
		CbmFile cbmFile = getCbmFile(false, false);
		Assert.assertEquals("file types", 6, CbmFile.getFileTypes().length);
		Assert.assertFalse(new CbmFile().toString().isEmpty());

		new CbmFile("FILENAME", FileType.PRG, 0, 1, 1, 1);
		Assert.assertEquals(cbmFile, new CbmFile(cbmFile));

		Assert.assertFalse("toString", cbmFile.toString().isEmpty());
		Assert.assertTrue("clone.equals", cbmFile.equals(new CbmFile(cbmFile)));

		Assert.assertEquals("compareTo", 0, cbmFile.compareTo(new CbmFile(cbmFile)));
		Assert.assertEquals("compareTo", 1, cbmFile.compareTo(new CbmFile(null, FileType.DEL, 0, 0, 0, 0)));
		Assert.assertEquals("compareTo", 0, new CbmFile(null, FileType.DEL, 0, 0, 0, 0).compareTo(new CbmFile(null, FileType.DEL, 0, 0, 0, 0)));
		Assert.assertEquals("compareTo", 1, new CbmFile("", FileType.DEL, 0, 0, 0, 0).compareTo(new CbmFile(null, FileType.DEL, 0, 0, 0, 0)));
		Assert.assertEquals("compareTo", -1, new CbmFile(null, FileType.DEL, 0, 0, 0, 0).compareTo(new CbmFile("", FileType.DEL, 0, 0, 0, 0)));

		Assert.assertEquals("hashCode", 47858214, cbmFile.hashCode());
		Assert.assertEquals("hashCode", 31, new CbmFile(null, FileType.PRG, 0, 1, 1, 1).hashCode());

		Assert.assertEquals(0,cbmFile.getGeos(0));
		Assert.assertEquals(0, cbmFile.getRelSector());
		Assert.assertEquals(0, cbmFile.getRelTrack());
		Assert.assertEquals(1, cbmFile.getDirTrack());
		Assert.assertEquals(1, cbmFile.getDirSector());
		Assert.assertEquals(0, cbmFile.getLsu());

		Assert.assertTrue("equals",cbmFile.equals(cbmFile));
		Assert.assertFalse("equals",cbmFile.equals(null));
		Assert.assertFalse("equals",cbmFile.equals(Integer.valueOf(1)));
		Assert.assertFalse("equals",cbmFile.equals(new CbmFile("FILENAME2", FileType.PRG, 0, 1, 1, 1)));
		Assert.assertFalse("equals",cbmFile.equals(new CbmFile(null, FileType.PRG, 0, 1, 1, 1)));
		Assert.assertFalse("equals",new CbmFile(null, FileType.PRG, 0, 1, 1, 1).equals(new CbmFile("NotNull", FileType.PRG, 0, 1, 1, 1)));
		Assert.assertTrue("equals",new CbmFile(null, FileType.PRG, 0, 1, 1, 1).equals(new CbmFile(null, FileType.PRG, 0, 1, 1, 1)));


	}

	@Test
	public void testAsDirString() {
		Assert.assertFalse("empty", new CbmFile().asDirString().isEmpty());
		Assert.assertFalse("PRG", new CbmFile("FILENAME", FileType.PRG, 0, 1, 1, 1).asDirString().isEmpty());
		//Assert.assertFalse("Unknown type", new CbmFile("FILENAME", Integer.MAX_VALUE, 0, 1, 1, 1).asDirString().isEmpty());
		CbmFile cbmFileLockedClosed = new CbmFile("FILENAME", FileType.PRG, 0, 1, 1, 1);
		cbmFileLockedClosed.setFileLocked(true);
		cbmFileLockedClosed.setFileClosed(true);
		Assert.assertFalse("LockedClosed", cbmFileLockedClosed.asDirString().isEmpty());
	}

	@Test
	public void testFileType() {
		Assert.assertEquals(FileType.PRG, CbmFile.getFileTypeFromFileExtension(null));
		Assert.assertNull(CbmFile.getFileType(Integer.MAX_VALUE));
	}

	@Test
	public void testToBytes() {
		CbmFile cbmFile = getCbmFile(false, false);

		cbmFile.toBytes(null, 0);
		cbmFile.toBytes(new byte[256], 1024);

		byte[] bytes = new byte[DiskImage.DIR_ENTRY_SIZE];
		cbmFile.toBytes(bytes, 0);
		CbmFile clone = new CbmFile(bytes, 0);
		cbmFile.toBytes(new byte[DiskImage.DIR_ENTRY_SIZE], 0);
		Assert.assertEquals("compareTo", 0, cbmFile.compareTo(clone));
		clone.setName(null);
		Assert.assertEquals("compareTo", 1, cbmFile.compareTo(clone));

		CbmFile cbmFile2 = getCbmFile(true, false);
		cbmFile2.toBytes(bytes, 0);

	}

	private CbmFile getCbmFile(boolean isLockedClosed, boolean isScratched) {
		CbmFile cbmFile = new CbmFile();
		cbmFile.setFileScratched(true);
		cbmFile.setFileType(FileType.PRG);
		cbmFile.setFileLocked(isLockedClosed);
		cbmFile.setFileClosed(isLockedClosed);
		cbmFile.setTrack(0);
		cbmFile.setSector(0);
		cbmFile.setName("FILENAME");
		cbmFile.setNameAsBytes("FILENAME".getBytes());
		cbmFile.setRelTrack(0);
		cbmFile.setRelSector(0);
		cbmFile.setSizeInBytes(2);
		cbmFile.setSizeInBlocks(1);
		cbmFile.setDirTrack(1);
		cbmFile.setDirSector(1);
		cbmFile.setDirPosition(1);

		return cbmFile;
	}
}