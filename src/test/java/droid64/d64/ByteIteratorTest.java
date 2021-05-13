package droid64.d64;

import org.junit.Assert;
import org.junit.Test;

public class ByteIteratorTest {

	@Test
	public void test() {
		byte[] data = {1,2,3,4,5,6,7};
		ByteIterator iter = new ByteIterator(data);
		int i=0;
		while (iter.hasNext()) {
			i++;
			iter.nextInt8();
		}
		Assert.assertEquals(7, i);
		Assert.assertFalse(iter.hasNext());
		Assert.assertFalse(iter.hasNextInt16());
	}

}
