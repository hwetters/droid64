package cfg;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.BooleanParameter;
import droid64.cfg.StringParameter;

public class BooleanParameterTest {

	@Test
	public void test() {
		var par = new BooleanParameter("UnitTest.Boolean");
		par.parse("true");
		Assert.assertEquals("","UnitTest.Boolean=true", par.toString());
		par.parse(null);
		Assert.assertEquals("","UnitTest.Boolean=false", par.toString());
		par.parse("false");
		Assert.assertEquals("","UnitTest.Boolean=false", par.toString());
		par.parse("yes");
		Assert.assertEquals("","UnitTest.Boolean=true", par.toString());
		par.parse("no");
		Assert.assertEquals("","UnitTest.Boolean=false", par.toString());
		par.setValue(true);
		Assert.assertEquals("","UnitTest.Boolean=true", par.toString());
		par.setValue(false);
		Assert.assertEquals("","UnitTest.Boolean=false", par.toString());


		par.reset();
		par.hashCode();
		Assert.assertFalse("", par.equals(null));
		Assert.assertTrue("", par.equals(par));
		Assert.assertTrue("", par.equals(new BooleanParameter("UnitTest.Boolean")));
		Assert.assertFalse("", par.equals(new BooleanParameter("UnitTest.Boolean2")));
		Assert.assertTrue("", par.equals(new StringParameter("UnitTest.Boolean")));
		Assert.assertEquals("", 0, par.compareTo(new BooleanParameter("UnitTest.Boolean")));
		par.getType();
	}
}
