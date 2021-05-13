package droid64.db;

import org.junit.Assert;
import org.junit.Test;

public class ObjectFactoryTest {

	@Test
	public void test() {


		Assert.assertNotNull("createDiskList", new ObjectFactory().createDiskList());
		Assert.assertNotNull("createDisk", new ObjectFactory().createDisk());
		Assert.assertNotNull("createDiskFile", new ObjectFactory().createDiskFile());
		Assert.assertNotNull("createBookmarkList", new ObjectFactory().createBookmarkList());
		Assert.assertNotNull("createBookmark", new ObjectFactory().createBookmark());

	}
}
