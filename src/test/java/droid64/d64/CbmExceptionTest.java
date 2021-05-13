package droid64.d64;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;


public class CbmExceptionTest {

	@Test
	public void test() {
		Assert.assertEquals("", "CbmException[ .message=Test1]", new CbmException("Test1").toString());
		new CbmException(new IOException("test2"));
		new CbmException("Test3", new IOException("test3"));
	}

}
