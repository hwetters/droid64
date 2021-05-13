package cfg;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.ListParameter;

public class ListParameterTest {
	@Test
	public void test() {
	var par = new ListParameter("UnitTest.ListParameter");
		par.setValue(Arrays.asList("aaa"));
		Assert.assertEquals("","UnitTest.ListParameter=aaa", par.toString());
		par.parse("a1;b2;c3");
		Assert.assertEquals("","UnitTest.ListParameter=a1;b2;c3", par.toString());
		par.parse(null);
		Assert.assertEquals("","UnitTest.ListParameter=", par.toString());
		par.setValue(null);
		Assert.assertEquals("","UnitTest.ListParameter=", par.toString());
		Assert.assertEquals("", 0, par.compareTo(new ListParameter("UnitTest.ListParameter")));
	}
}
