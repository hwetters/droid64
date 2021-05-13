package cfg;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.FileParameter;

public class FileParameterTest {
	@Test
	public void test() {
	var par = new FileParameter("UnitTest.File");
		par.parse("1213");
		Assert.assertEquals("","UnitTest.File=1213", par.toString());
		par.parse("xx");
		Assert.assertEquals("","UnitTest.File=xx", par.toString());
		par.parse("yy");
		Assert.assertEquals("","UnitTest.File=yy", par.toString());
		par.parse(null);
		Assert.assertEquals("","UnitTest.File=", par.toString());
	}
}
