package droid64.d64;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;


public class CyclicGuardTest {

	@Test
	public void test() {
		Assert.assertFalse("empty", new CyclicGuard<Integer>().contains(1));
		Assert.assertEquals("empty", 0, new CyclicGuard<Integer>().size());
		Assert.assertEquals("empty", "", new CyclicGuard<Integer>().toString());
		Assert.assertTrue("empty", new CyclicGuard<Integer>().addSilent(1));
	}

	@Test(expected = CbmException.class)
	public void testGuard() throws CbmException {
		CyclicGuard<FileType> guard = new CyclicGuard<>();
		guard.add(FileType.CBM);
		Assert.assertFalse("cbm", guard.addSilent(FileType.CBM));
		guard.add(FileType.SEQ);
		Assert.assertTrue("prg", guard.addSilent(FileType.PRG));
		Assert.assertFalse("prg", guard.addSilent(FileType.PRG));
		Assert.assertTrue("rel", guard.addSilent(FileType.REL));

		Assert.assertTrue("rel", guard.contains(FileType.CBM));
		Assert.assertFalse("del", guard.contains(FileType.DEL));

		Assert.assertEquals("size", 4, guard.size());

		Assert.assertEquals("stream", 4, guard.stream().collect(Collectors.toList()).size());

		guard.add(FileType.CBM);
	}
}
