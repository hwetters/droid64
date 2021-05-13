package cfg;

import java.awt.Font;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.FontParameter;
import droid64.cfg.IndexedStringParameter;

public class FontParameterTest {

	@Test
	public void test() {
	var par = new FontParameter("UnitTest.FontParameter");
		par.setValue(new Font("Courier", Font.BOLD, 10));
		Assert.assertEquals("","UnitTest.FontParameter=Courier;1;10", par.toString());
		par.parse("Times;0;8");
		Assert.assertEquals("","UnitTest.FontParameter=Times;0;8", par.toString());
		par.parse(null);
		Assert.assertEquals("","UnitTest.FontParameter=", par.toString());
		par.setValue(null);
		Assert.assertEquals("","UnitTest.FontParameter=", par.toString());
		par.parse("");
		Assert.assertEquals("","UnitTest.FontParameter=", par.toString());
		Assert.assertEquals("", 0, par.compareTo(new IndexedStringParameter("UnitTest.FontParameter")));
	}

}
