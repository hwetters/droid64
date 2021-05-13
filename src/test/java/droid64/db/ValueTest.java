package droid64.db;

import org.junit.Assert;
import org.junit.Test;

public class ValueTest {

	@Test
	public void testInit() {
		Assert.assertTrue("init insert", new Value().isInsert());
		Assert.assertFalse("init clean", new Value().isClean());
		Assert.assertFalse("init delete", new Value().isDelete());
		Assert.assertFalse("init update", new Value().isUpdate());
		Assert.assertEquals("init update", "insert", new Value().getState());
	}

	@Test
	public void testSetState() {
		var value = new Value();
		Assert.assertTrue("setState default", value.isInsert());
		Assert.assertEquals("setState default", "insert", value.getState());
		value.setClean();
		Assert.assertTrue("setState clean", value.isClean());
		Assert.assertEquals("setState default", "clean", value.getState());
		value.setDelete();
		Assert.assertTrue("setState delete", value.isDelete());
		Assert.assertEquals("setState default", "delete", value.getState());
		value.setInsert();
		Assert.assertTrue("setState insert", value.isInsert());
		Assert.assertEquals("setState default", "insert", value.getState());
		value.setUpdate();
		Assert.assertTrue("setUpdate update", value.isUpdate());
		Assert.assertEquals("setState default", "update", value.getState());
	}
}
