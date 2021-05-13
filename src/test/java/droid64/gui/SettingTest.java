package droid64.gui;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import droid64.d64.DiskImageType;
import droid64.d64.Utility;
public class SettingTest {
	private static final Date TIMESTAMP = parseDateString("2020-04-10 10:20:30");

	private static final File CONF_FILE = new File("src/test/resources/droid64/gui/test.config");
	@Test
	public void testLoad() throws IOException {

        final String orgConfig = new String (Files.readAllBytes(CONF_FILE.toPath()));
		Setting.resetAll();
		Setting.load(new File("src/test/resources/droid64/gui/test.config"));
		Assert.assertEquals("Setting count", 55, Setting.values().length);
        StringWriter out = new StringWriter();
        Setting.save(new PrintWriter(out), TIMESTAMP);
        out.flush();
        String outConfig = out.toString();
        Assert.assertEquals("Compare org config string with saved string", outConfig,  orgConfig);
        Setting.setExternalProgram(0, Setting.getExternalProgram(0));


	}

	@Test
	public void testSave() throws Exception {
		File saveFile = File.createTempFile("UnitTest_config_", ".cfg");
		saveFile.deleteOnExit();
		Setting.save(saveFile);
		Assert.assertTrue("file exists", saveFile.isFile());
	}

	@Test
	public void testGetMessage() {
		Assert.assertNull("null", Utility.getMessage(null));
		Assert.assertEquals("missing", "TestMissingKey", Utility.getMessage("TestMissingKey"));
		Assert.assertEquals("Existing", "Program", Utility.getMessage("droid64.menu.program"));
	}
	@Test
	public void testBoolean() {
		Assert.assertNotNull("notNull", Setting.ASK_QUIT.getBoolean());
		Assert.assertEquals("default", Boolean.FALSE, Setting.ASK_QUIT.getBoolean());
		Setting.ASK_QUIT.set(Boolean.TRUE);
		Assert.assertEquals("changed", Boolean.TRUE, Setting.ASK_QUIT.getBoolean());
	}

	private static Date parseDateString(String str) {
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
			Date x = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
			System.out.println("x="+x+"  y=" +new Date());
			return x;
		} catch (ParseException e) {
			return null;
		}
	}

	@Test
	public void testGetImageType() throws Exception {
		Setting.load(new File("src/test/resources/droid64/gui/test.config"));
		Assert.assertEquals("D64", DiskImageType.D64, Setting.getDiskImageType(new File("TEST.D64.GZ")));
		Assert.assertEquals("d64", DiskImageType.D64, Setting.getDiskImageType(new File("test.d64")));
		Assert.assertEquals("D64", DiskImageType.D64, Setting.getDiskImageType(new File("TEST.D64")));
		Assert.assertEquals("s64.gz", DiskImageType.D64, Setting.getDiskImageType(new File("test.d64.gz")));
		Assert.assertEquals("d67", DiskImageType.D67, Setting.getDiskImageType(new File("test.d67")));
		Assert.assertEquals("d71", DiskImageType.D71, Setting.getDiskImageType(new File("test.d71")));
		Assert.assertEquals("d80", DiskImageType.D80, Setting.getDiskImageType(new File("test.d80")));
		Assert.assertEquals("d81", DiskImageType.D81, Setting.getDiskImageType(new File("test.d81")));
		Assert.assertEquals("d82", DiskImageType.D82, Setting.getDiskImageType(new File("test.d82")));
		Assert.assertEquals("d88", DiskImageType.D88, Setting.getDiskImageType(new File("test.d88")));
		Assert.assertEquals("lnx", DiskImageType.LNX, Setting.getDiskImageType(new File("test.lnx")));
		Assert.assertEquals("t64", DiskImageType.T64, Setting.getDiskImageType(new File("test.t64")));
		Assert.assertEquals("xxx", DiskImageType.UNDEFINED, Setting.getDiskImageType(new File("test.xxx")));
	}

	@Test
	public void testCheckFileNameExtension() throws Exception {
		Setting.load(new File("src/test/resources/droid64/gui/test.config"));
		Assert.assertEquals("test.t64", Setting.checkFileNameExtension(DiskImageType.T64, false, "test.t64"));
		Assert.assertEquals("test.t64.gz", Setting.checkFileNameExtension(DiskImageType.T64, true, "test.t64.gz"));
		Assert.assertEquals("test.txt.t64", Setting.checkFileNameExtension(DiskImageType.T64, false, "test.txt"));
		Assert.assertEquals("test.txt.t64.gz", Setting.checkFileNameExtension(DiskImageType.T64, true, "test.txt.t64.gz"));
		Setting.checkFileNameExtension(DiskImageType.D64, false, "");
		Setting.checkFileNameExtension(null, false, "");
		Setting.checkFileNameExtension(DiskImageType.UNDEFINED, false, null);
	}

	@Test
	public void testLoadSettings() throws Exception {
		Map<String, String> map = Setting.loadSettings(new FileReader(new File("src/test/resources/droid64/gui/test.config")));
		Assert.assertNotNull("null map", map);
		Assert.assertFalse("empty map", map.isEmpty());
	}

}
