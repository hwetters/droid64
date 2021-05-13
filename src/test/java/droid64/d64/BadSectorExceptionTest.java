package droid64.d64;

import org.junit.Assert;
import org.junit.Test;

public class BadSectorExceptionTest {

	@Test
	public void test() {
		BadSectorException ex1 = new BadSectorException("UnitTest",1,2);
		Assert.assertEquals("BadSectorTest", "UnitTest [1/2]", ex1.getMessage());
	}
}
