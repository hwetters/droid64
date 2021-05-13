package cfg;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.IndexedStringParameter;

public class IndexedStringParameterTest {
	@Test
	public void test() {
	var par = new IndexedStringParameter("UnitTest.IndexedString");
		par.setValue(Arrays.asList("aaa"));
		Assert.assertEquals("","UnitTest.IndexedString=aaa", par.toString());
		par.parse("abcd");
		Assert.assertEquals("","UnitTest.IndexedString=aaa;abcd", par.toString());
		par.parse(null);
		Assert.assertEquals("","UnitTest.IndexedString=aaa;abcd", par.toString());
		par.setValue(null);
		Assert.assertEquals("","UnitTest.IndexedString=", par.toString());
		Assert.assertEquals("", 0, par.compareTo(new IndexedStringParameter("UnitTest.IndexedString")));
	}
}
