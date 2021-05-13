package droid64.d64;

import org.junit.Assert;
import org.junit.Test;

public class TrackSectorTest {

	@Test
	public void test() {
		Assert.assertTrue(new TrackSector(1,1).equals(new TrackSector(1,1)));
		Assert.assertFalse(new TrackSector(1,1).equals(new TrackSector(1,2)));
		Assert.assertFalse(new TrackSector(1,1).toString().isEmpty());

		StringBuilder buf = new StringBuilder();
		new TrackSector(1,1).toString(buf);
		Assert.assertFalse(buf.toString().isEmpty());

		Assert.assertEquals(new TrackSector(1,1).hashCode(), new TrackSector(1,1).hashCode());
	}
}
