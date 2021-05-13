package droid64.gui;

import org.junit.Assert;
import org.junit.Test;

import droid64.d64.DiskImageType;
import droid64.d64.FileType;

public class RenameResultTest {

	@Test
	public void test() {
		RenameResult result = new RenameResult();
		result.setCompressedDisk(false);
		result.setCpmDisk(false);
		result.setDiskID("id");
		result.setDiskName("name");
		result.setDiskType(DiskImageType.D64);
		result.setFileName("filename");
		result.setFileType(FileType.SEQ);
		result.getDiskID();
		result.getDiskName();
		result.getDiskType();
		result.getFileType();
		Assert.assertNotNull("",result.getFileName());
		result.isCompressedDisk();
		result.isCpmDisk();
	}

}
