package droid64.d64;

import org.junit.Assert;
import org.junit.Test;

public class CbmBamTest {

	@Test
	public void testdump() {
		CbmBam bam = new CbmBam(16, 5+1);
		for (int t=0; t<16;t++) {
			for (int n=0;n<5;n++) {
				bam.setTrackBits(t+1, n+1, n);
			}
		}
		Assert.assertNotNull("", bam.dump());
		Assert.assertNotNull("", bam.toString());
		Assert.assertNotNull("", new CbmBam(16, 1).dump());
		Assert.assertNotNull("", new CbmBam(0, 1).dump());
	}

}
