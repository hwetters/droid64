package droid64.d64;

import org.junit.Assert;
import org.junit.Test;

public class CpmFileTest {

	@Test
	public void test() {
		CpmFile cpmFile = new CpmFile();
		cpmFile.setFileScratched(true);
		cpmFile.setFileType(FileType.PRG);
		cpmFile.setFileLocked(false);
		cpmFile.setFileClosed(false);
		cpmFile.setTrack(0);
		cpmFile.setSector(0);
		cpmFile.setName("FILENAME.TXT");
		cpmFile.setRelTrack(0);
		cpmFile.setRelSector(0);
		cpmFile.setSizeInBytes(2);
		cpmFile.setSizeInBlocks(1);
		cpmFile.setDirTrack(1);
		cpmFile.setDirSector(1);
		cpmFile.setDirPosition(1);

		cpmFile.addAllocUnit(1);
		Assert.assertEquals(1, cpmFile.getAllocList().size());
		Assert.assertEquals(cpmFile, new CpmFile(cpmFile));
		Assert.assertEquals("FILENAME.TXT", cpmFile.getCpmNameAndExt());
		cpmFile.setCpmName("AAA");
		cpmFile.setCpmNameExt("BBB");
		Assert.assertEquals("aaa.bbb", cpmFile.getCpmNameAndExt());
		Assert.assertTrue(cpmFile.equals(cpmFile));
		Assert.assertEquals(Long.valueOf(-1692861641).longValue(), Long.valueOf(cpmFile.hashCode()).longValue());

		Assert.assertFalse(new CpmFile().toString().isEmpty());
		Assert.assertFalse(new CpmFile().asDirString().isEmpty());
	}

}
