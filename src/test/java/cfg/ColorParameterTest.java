package cfg;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.ColorParameter;

public class ColorParameterTest {

	@Test
	public void test() {
		var par = new ColorParameter("UnitTest.ColorParameter");
		par.parse("11;22;33");
		Assert.assertEquals("","UnitTest.ColorParameter=11,22,33", par.toString());
		par.setValue(0xff102030);
		Assert.assertEquals("","UnitTest.ColorParameter=16,32,48", par.toString());
		par.setValue(Color.BLUE);
		Assert.assertEquals("","UnitTest.ColorParameter=0,0,255", par.toString());
		par.parse(null);
		Assert.assertEquals("","UnitTest.ColorParameter=", par.toString());
		par.parse("11,22");
		Assert.assertEquals("","UnitTest.ColorParameter=", par.toString());
	}
}