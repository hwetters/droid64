package droid64.gui;

import org.junit.Assert;
import org.junit.Test;

public class BugsPanelTest {

	@Test
	public void test() {
		new BugsPanel();
		Assert.assertFalse(BugsPanel.getBugs().isEmpty());
	}

}
