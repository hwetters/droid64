package cfg;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.IntegerParameter;

public class IntegerParameterTest {
	@Test
	public void test() {
	var par = new IntegerParameter("UnitTest.Integer");
		par.parse("1213");
		Assert.assertEquals("","UnitTest.Integer=1213", par.toString());
		par.setValue(11);
		Assert.assertEquals("","UnitTest.Integer=11", par.toString());
		par.setValue(22);
		Assert.assertEquals("","UnitTest.Integer=22", par.toString());
		par.parse(null);
		Assert.assertEquals("","UnitTest.Integer=", par.toString());
	}
}
