package droid64.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import droid64.d64.DiskImageType;

public class ExternalProgramTest {

	@Test
	public void test() throws IOException {

		File tmpFile = File.createTempFile("UnitTest_", ".d64");
		tmpFile.deleteOnExit();

		File targetFile = File.createTempFile("UnitTest_", ".tmp");
		targetFile.deleteOnExit();

		ExternalProgram prg = new ExternalProgram("cmd", "{Image} {Files} {ImageFiles} {Target} {ImageType} {DriveType} EXTRA", "description", "label", false);
		List<String> imageFiles = Arrays.asList(new String[] {"10", "11", "12" });
		List<String> args = prg.getExecute(tmpFile, imageFiles, targetFile, tmpFile.getParentFile(), DiskImageType.D64);

		final String expected = String.format("cmd, %s, 10, 11, 12, %s:10, %s:11, %s:12, %s, D64, 1541, EXTRA", tmpFile, tmpFile, tmpFile, tmpFile, targetFile);

		Assert.assertEquals("test", expected, args.stream().collect(Collectors.joining(", ")));
		Assert.assertFalse(prg.toString().isEmpty());
		prg.setValues("cmd", "args", "desc", "label", false);
		prg.getExecute(tmpFile, imageFiles, targetFile, tmpFile.getParentFile(), DiskImageType.T64);

		prg.setArguments("-l");
		prg.setCommand("/bin/ls");
		prg.setDescription("descr");
		prg.setLabel("lbl");
		prg.setForkThread(false);


		Assert.assertEquals("empty command", 0,
				new ExternalProgram("", "args", "description", "label", false)
					.getExecute(new File("1"), Arrays.asList("x"), new File("2"), new File("dir"), DiskImageType.D64).size());
		Assert.assertEquals("null command", 0,
				new ExternalProgram(null, "args", "description", "label", false)
					.getExecute(new File("1"), Arrays.asList("x"), new File("2"), new File("dir"), DiskImageType.D64).size());

		Assert.assertEquals("null source files", 2,
				new ExternalProgram("cmd", "args", "description", "label", false)
					.getExecute(new File("1"), null, new File("2"), new File("dir"), DiskImageType.D64).size());

		Assert.assertEquals("empty source files", 2,
				new ExternalProgram("cmd", "args", "description", "label", false)
					.getExecute(new File("1"), new ArrayList<String>(), new File("2"), new File("dir"), DiskImageType.D64).size());

	}

}
