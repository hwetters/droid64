package cfg;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.StringParameter;

public class StringParameterTest {
	@Test
	public void test() {
	var par = new StringParameter("UnitTest.String");
		par.parse("1213");
		Assert.assertEquals("","UnitTest.String=1213", par.toString());
		par.setValue("abcd");
		Assert.assertEquals("","UnitTest.String=abcd", par.toString());
		par.setValue("");
		Assert.assertEquals("","UnitTest.String=", par.toString());
	}
}
